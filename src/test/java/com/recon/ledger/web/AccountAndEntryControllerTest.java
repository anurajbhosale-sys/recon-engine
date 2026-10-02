package com.recon.ledger.web;

import com.jayway.jsonpath.JsonPath;
import com.recon.TestcontainersConfiguration;
import com.recon.ledger.Account;
import com.recon.ledger.AccountRepository;
import com.recon.ledger.AccountType;
import com.recon.tenancy.Tenant;
import com.recon.tenancy.TenantRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@Import(TestcontainersConfiguration.class)
class AccountAndEntryControllerTest {

    @Autowired WebApplicationContext context;
    @Autowired TenantRepository tenantRepository;
    @Autowired AccountRepository accountRepository;

    MockMvc mockMvc;
    UUID tenantId;
    Account receivable;
    Account fees;
    Account revenue;

    @BeforeEach
    void setUp() {
        // CONCEPT: build MockMvc from the full running app context (controllers, handler, everything)
        mockMvc = MockMvcBuilders.webAppContextSetup(context).build();

        tenantId = tenantRepository.save(new Tenant("Chai Co")).getId();
        receivable = accountRepository.save(new Account(tenantId, "processor_receivable", "Stripe receivable", AccountType.ASSET, "USD"));
        fees = accountRepository.save(new Account(tenantId, "processing_fees", "Processing fees", AccountType.EXPENSE, "USD"));
        revenue = accountRepository.save(new Account(tenantId, "revenue", "Revenue", AccountType.REVENUE, "USD"));
    }

    // The $80 sale as JSON. %s placeholders get filled with this test's account IDs.
    private String saleJson() {
        return """
                {"effectiveDate":"2026-10-01","description":"Order 1001","lines":[
                  {"accountId":"%s","direction":"DEBIT","amountMinor":7738,"currency":"USD"},
                  {"accountId":"%s","direction":"DEBIT","amountMinor":262,"currency":"USD"},
                  {"accountId":"%s","direction":"CREDIT","amountMinor":8000,"currency":"USD"}]}
                """.formatted(receivable.getId(), fees.getId(), revenue.getId());
    }

    private String entriesUrl() {
        return "/tenants/" + tenantId + "/journal-entries";
    }

    @Test
    void newEntryReturns201WithLocation() throws Exception {
        mockMvc.perform(post(entriesUrl())
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Idempotency-Key", "charge-1001")
                        .content(saleJson()))
                .andExpect(status().isCreated())                                        // 201
                .andExpect(header().string("Location", containsString("/journal-entries/")))
                .andExpect(jsonPath("$.created").value(true));
    }

    @Test
    void duplicateReturns200WithSameId() throws Exception {
        String first = mockMvc.perform(post(entriesUrl())
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Idempotency-Key", "charge-1001")
                        .content(saleJson()))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        String firstId = JsonPath.read(first, "$.entryId");   // CONCEPT: pull one field out of the JSON

        mockMvc.perform(post(entriesUrl())
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Idempotency-Key", "charge-1001")
                        .content(saleJson()))
                .andExpect(status().isOk())                                             // 200
                .andExpect(jsonPath("$.created").value(false))
                .andExpect(jsonPath("$.entryId").value(firstId));
    }

    @Test
    void unbalancedReturns400ProblemDetail() throws Exception {
        String unbalanced = """
                {"effectiveDate":"2026-10-01","lines":[
                  {"accountId":"%s","direction":"DEBIT","amountMinor":7738,"currency":"USD"},
                  {"accountId":"%s","direction":"CREDIT","amountMinor":8000,"currency":"USD"}]}
                """.formatted(receivable.getId(), revenue.getId());

        mockMvc.perform(post(entriesUrl())
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Idempotency-Key", "bad-1")
                        .content(unbalanced))
                .andExpect(status().isBadRequest())                                     // 400
                .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.title").value("Invalid journal entry"))
                .andExpect(jsonPath("$.detail", containsString("Unbalanced")));
    }

    @Test
    void missingIdempotencyKeyReturns400() throws Exception {
        mockMvc.perform(post(entriesUrl())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(saleJson()))                                           // no header
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detail", containsString("Idempotency-Key")));
    }

    @Test
    void invalidBodyReturns400() throws Exception {
        String negative = """
                {"effectiveDate":"2026-10-01","lines":[
                  {"accountId":"%s","direction":"DEBIT","amountMinor":-5,"currency":"USD"},
                  {"accountId":"%s","direction":"CREDIT","amountMinor":-5,"currency":"USD"}]}
                """.formatted(receivable.getId(), revenue.getId());

        mockMvc.perform(post(entriesUrl())
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Idempotency-Key", "neg-1")
                        .content(negative))
                .andExpect(status().isBadRequest());     // CONCEPT: @Positive rejects it before the service runs
    }

    @Test
    void balanceReturns200() throws Exception {
        mockMvc.perform(post(entriesUrl())
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Idempotency-Key", "charge-1001")
                        .content(saleJson()))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/tenants/" + tenantId + "/accounts/" + receivable.getId() + "/balance"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.balanceMinor").value(7738))
                .andExpect(jsonPath("$.currency").value("USD"));
    }

    @Test
    void otherTenantsAccountReturns404() throws Exception {
        UUID otherTenant = tenantRepository.save(new Tenant("Book Nook")).getId();

        // Book Nook asks for Chai Co's receivable → must look like it doesn't exist
        mockMvc.perform(get("/tenants/" + otherTenant + "/accounts/" + receivable.getId() + "/balance"))
                .andExpect(status().isNotFound())                                       // 404
                .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON));
    }

    @Test
    void secondReversalReturns409() throws Exception {
        String created = mockMvc.perform(post(entriesUrl())
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Idempotency-Key", "charge-1001")
                        .content(saleJson()))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        String entryId = JsonPath.read(created, "$.entryId");

        mockMvc.perform(post(entriesUrl() + "/" + entryId + "/reversal")
                        .header("Idempotency-Key", "reverse-a"))
                .andExpect(status().isCreated());                                       // first: 201

        mockMvc.perform(post(entriesUrl() + "/" + entryId + "/reversal")
                        .header("Idempotency-Key", "reverse-b"))                        // different key
                .andExpect(status().isConflict())                                       // second: 409
                .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.detail", containsString("already been reversed")));
    }
}