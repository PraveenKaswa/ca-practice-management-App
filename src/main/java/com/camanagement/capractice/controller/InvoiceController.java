package com.camanagement.capractice.controller;

import com.camanagement.capractice.entity.*;
import com.camanagement.capractice.repository.*;
import com.camanagement.capractice.service.InvoiceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

/**
 * INVOICE CONTROLLER:
 *
 * Handles all invoice-related web requests
 * Follows same pattern as DashboardController
 */
@Controller
@RequestMapping("/invoices")
public class InvoiceController {

    @Autowired
    private InvoiceService invoiceService;

    @Autowired
    private InvoiceRepository invoiceRepository;

    @Autowired
    private ClientRepository clientRepository;

    @Autowired
    private ClientServiceRepository clientServiceRepository;

    /**
     * LIST ALL INVOICES:
     *
     * GET /invoices
     * Show all invoices with filtering options
     */
    @GetMapping
    public String listInvoices(
            @RequestParam(value = "status", required = false) String status,
            @RequestParam(value = "clientId", required = false) Long clientId,
            Model model) {

        try {
            System.out.println("=== LOADING INVOICES LIST ===");
            System.out.println("Status filter: " + status);
            System.out.println("Client filter: " + clientId);

            List<Invoice> invoices;

            // Apply filters
            if (status != null && !status.isEmpty() && !status.equals("ALL")) {
                Invoice.InvoiceStatus statusEnum = Invoice.InvoiceStatus.valueOf(status);
                invoices = invoiceRepository.findByStatus(statusEnum);
            } else if (clientId != null) {
                invoices = invoiceRepository.findByClientId(clientId);
            } else {
                invoices = invoiceRepository.findAllByOrderByInvoiceDateDesc();
            }

            // Get statistics
            InvoiceService.InvoiceStatistics stats = invoiceService.getInvoiceStatistics();

            // Add to model
            model.addAttribute("invoices", invoices);
            model.addAttribute("stats", stats);
            model.addAttribute("selectedStatus", status);
            model.addAttribute("selectedClientId", clientId);

            // Get all clients for filter dropdown
            List<Client> allClients = clientRepository.findByStatusOrderByClientNameAsc(Client.ClientStatus.ACTIVE);
            model.addAttribute("allClients", allClients);

            System.out.println("Loaded " + invoices.size() + " invoices");

            return "invoices";

        } catch (Exception e) {
            System.err.println("ERROR loading invoices: " + e.getMessage());
            e.printStackTrace();
            model.addAttribute("error", "Failed to load invoices");
            return "invoices";
        }
    }

    /**
     * SHOW CREATE INVOICE FORM:
     *
     * GET /invoices/create?clientId=123
     * Display form to select services and create invoice
     */
    @GetMapping("/create")
    public String showCreateInvoiceForm(@RequestParam(value = "clientId", required = false) Long clientId,
                                        Model model) {
        try {
            System.out.println("=== SHOWING CREATE INVOICE FORM ===");

            if (clientId != null) {
                // Client pre-selected
                Client client = clientRepository.findById(clientId)
                        .orElseThrow(() -> new RuntimeException("Client not found"));

                // Get completed services for this client
                List<ClientService> completedServices = clientServiceRepository
                        .findByClientAndStatus(client, ClientService.ServiceStatus.COMPLETED);

                model.addAttribute("selectedClient", client);
                model.addAttribute("completedServices", completedServices);

                System.out.println("Client: " + client.getClientName());
                System.out.println("Completed services: " + completedServices.size());
            }

            // Get all active clients for dropdown
            List<Client> allClients = clientRepository.findByStatusOrderByClientNameAsc(Client.ClientStatus.ACTIVE);
            model.addAttribute("allClients", allClients);

            // Default values
            model.addAttribute("defaultTaxRate", new BigDecimal("18.00"));
            model.addAttribute("defaultPaymentTerms", 15);

            return "create-invoice";

        } catch (Exception e) {
            System.err.println("ERROR showing create invoice form: " + e.getMessage());
            e.printStackTrace();
            return "redirect:/invoices?error=createform";
        }
    }

    /**
     * CREATE INVOICE:
     *
     * POST /invoices/create
     * Process the form and create invoice
     */
    @PostMapping("/create")
    public String createInvoice(@RequestParam("clientId") Long clientId,
                                @RequestParam("serviceIds") List<Long> serviceIds,
                                @RequestParam(value = "taxPercentage", required = false) BigDecimal taxPercentage,
                                @RequestParam(value = "discountPercentage", required = false) BigDecimal discountPercentage,
                                @RequestParam(value = "paymentTermDays", required = false) Integer paymentTermDays,
                                Model model) {
        try {
            System.out.println("=== CREATING INVOICE ===");
            System.out.println("Client ID: " + clientId);
            System.out.println("Service IDs: " + serviceIds);

            // Validate input
            if (serviceIds == null || serviceIds.isEmpty()) {
                model.addAttribute("error", "Please select at least one service");
                return showCreateInvoiceForm(clientId, model);
            }

            // Create invoice
            Invoice invoice = invoiceService.createInvoiceFromServices(
                    clientId,
                    serviceIds,
                    taxPercentage,
                    discountPercentage,
                    paymentTermDays
            );

            System.out.println("Invoice created: " + invoice.getInvoiceNumber());
            System.out.println("=== INVOICE CREATED SUCCESSFULLY ===");

            // Redirect to view the created invoice
            return "redirect:/invoices/" + invoice.getId() + "/view?created=true";

        } catch (Exception e) {
            System.err.println("ERROR creating invoice: " + e.getMessage());
            e.printStackTrace();
            model.addAttribute("error", "Failed to create invoice: " + e.getMessage());
            return showCreateInvoiceForm(clientId, model);
        }
    }

    /**
     * VIEW INVOICE DETAILS:
     *
     * GET /invoices/{id}/view
     * Display complete invoice with all details
     */
    @GetMapping("/{id}/view")
    public String viewInvoice(@PathVariable("id") Long id, Model model) {
        try {
            System.out.println("=== VIEWING INVOICE ===");
            System.out.println("Invoice ID: " + id);

            // Find invoice
            Invoice invoice = invoiceRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Invoice not found"));

            // Add to model
            model.addAttribute("invoice", invoice);
            model.addAttribute("client", invoice.getClient());
            model.addAttribute("items", invoice.getItems());

            // Calculate additional details
            model.addAttribute("outstandingAmount", invoice.getOutstandingAmount());
            model.addAttribute("isOverdue", invoice.isOverdue());
            model.addAttribute("isPartiallyPaid", invoice.isPartiallyPaid());

            System.out.println("Invoice: " + invoice.getInvoiceNumber());
            System.out.println("Client: " + invoice.getClient().getClientName());
            System.out.println("Total: ₹" + invoice.getTotalAmount());

            return "view-invoice";

        } catch (Exception e) {
            System.err.println("ERROR viewing invoice: " + e.getMessage());
            e.printStackTrace();
            return "redirect:/invoices?error=notfound";
        }
    }

    /**
     * SHOW RECORD PAYMENT FORM:
     *
     * GET /invoices/{id}/payment
     * Display form to record payment
     */
    @GetMapping("/{id}/payment")
    public String showRecordPaymentForm(@PathVariable("id") Long id, Model model) {
        try {
            System.out.println("=== SHOWING PAYMENT FORM ===");

            Invoice invoice = invoiceRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Invoice not found"));

            // Check if invoice can accept payment
            if (invoice.getStatus() == Invoice.InvoiceStatus.PAID) {
                model.addAttribute("error", "This invoice is already paid");
                return "redirect:/invoices/" + id + "/view?error=alreadypaid";
            }

            if (invoice.getStatus() == Invoice.InvoiceStatus.CANCELLED) {
                model.addAttribute("error", "Cannot record payment for cancelled invoice");
                return "redirect:/invoices/" + id + "/view?error=cancelled";
            }

            model.addAttribute("invoice", invoice);
            model.addAttribute("outstandingAmount", invoice.getOutstandingAmount());
            model.addAttribute("paymentMethods", Invoice.PaymentMethod.values());

            return "record-payment";

        } catch (Exception e) {
            System.err.println("ERROR showing payment form: " + e.getMessage());
            return "redirect:/invoices?error=paymentform";
        }
    }

    /**
     * RECORD PAYMENT:
     *
     * POST /invoices/{id}/payment
     * Process payment recording
     */
    @PostMapping("/{id}/payment")
    public String recordPayment(@PathVariable("id") Long id,
                                @RequestParam("amount") BigDecimal amount,
                                @RequestParam("paymentMethod") String paymentMethodStr,
                                @RequestParam(value = "paymentReference", required = false) String paymentReference,
                                Model model) {
        try {
            System.out.println("=== RECORDING PAYMENT ===");
            System.out.println("Invoice ID: " + id);
            System.out.println("Amount: ₹" + amount);

            // Parse payment method
            Invoice.PaymentMethod paymentMethod = Invoice.PaymentMethod.valueOf(paymentMethodStr);

            // Record payment
            Invoice updatedInvoice = invoiceService.recordPayment(id, amount, paymentMethod, paymentReference);

            System.out.println("Payment recorded successfully");
            System.out.println("New status: " + updatedInvoice.getStatus());

            return "redirect:/invoices/" + id + "/view?payment=success";

        } catch (Exception e) {
            System.err.println("ERROR recording payment: " + e.getMessage());
            e.printStackTrace();
            model.addAttribute("error", "Failed to record payment: " + e.getMessage());
            return showRecordPaymentForm(id, model);
        }
    }

    /**
     * SEND INVOICE:
     *
     * POST /invoices/{id}/send
     * Mark invoice as SENT
     */
    @PostMapping("/{id}/send")
    public String sendInvoice(@PathVariable("id") Long id) {
        try {
            System.out.println("=== SENDING INVOICE ===");

            invoiceService.sendInvoice(id);

            System.out.println("Invoice marked as SENT");

            return "redirect:/invoices/" + id + "/view?sent=true";

        } catch (Exception e) {
            System.err.println("ERROR sending invoice: " + e.getMessage());
            return "redirect:/invoices/" + id + "/view?error=send";
        }
    }

    /**
     * CANCEL INVOICE:
     *
     * POST /invoices/{id}/cancel
     * Cancel an invoice
     */
    @PostMapping("/{id}/cancel")
    public String cancelInvoice(@PathVariable("id") Long id,
                                @RequestParam(value = "reason", required = false) String reason) {
        try {
            System.out.println("=== CANCELLING INVOICE ===");

            invoiceService.cancelInvoice(id, reason);

            System.out.println("Invoice cancelled");

            return "redirect:/invoices/" + id + "/view?cancelled=true";

        } catch (Exception e) {
            System.err.println("ERROR cancelling invoice: " + e.getMessage());
            return "redirect:/invoices/" + id + "/view?error=cancel";
        }
    }

    /**
     * GET COMPLETED SERVICES FOR CLIENT (AJAX):
     *
     * GET /invoices/client/{clientId}/services
     * Used by JavaScript to populate service selection
     */
    @GetMapping("/client/{clientId}/services")
    @ResponseBody
    public List<ClientServiceDTO> getCompletedServices(@PathVariable("clientId") Long clientId) {
        try {
            Client client = clientRepository.findById(clientId)
                    .orElseThrow(() -> new RuntimeException("Client not found"));

            List<ClientService> completedServices = clientServiceRepository
                    .findByClientAndStatus(client, ClientService.ServiceStatus.COMPLETED);

            // Convert to DTO for JSON response
            return completedServices.stream()
                    .map(cs -> new ClientServiceDTO(
                            cs.getId(),
                            cs.getService().getServiceName(),
                            cs.getQuotedPrice(),
                            cs.getCompletionDate()
                    ))
                    .collect(Collectors.toList());

        } catch (Exception e) {
            System.err.println("ERROR fetching services: " + e.getMessage());
            return List.of();
        }
    }

    /**
     * INVOICE STATISTICS (for dashboard integration):
     *
     * GET /invoices/stats
     */
    @GetMapping("/stats")
    @ResponseBody
    public InvoiceService.InvoiceStatistics getStatistics() {
        return invoiceService.getInvoiceStatistics();
    }

    /**
     * DTO CLASS: Client Service Response
     * For AJAX responses
     */
    public static class ClientServiceDTO {
        private Long id;
        private String serviceName;
        private BigDecimal price;
        private LocalDate completionDate;

        public ClientServiceDTO(Long id, String serviceName, BigDecimal price, LocalDate completionDate) {
            this.id = id;
            this.serviceName = serviceName;
            this.price = price;
            this.completionDate = completionDate;
        }

        // Getters
        public Long getId() { return id; }
        public String getServiceName() { return serviceName; }
        public BigDecimal getPrice() { return price; }
        public LocalDate getCompletionDate() { return completionDate; }
    }
}