package com.camanagement.capractice.config;

import com.camanagement.capractice.entity.Client;
import com.camanagement.capractice.entity.ClientService;
import com.camanagement.capractice.entity.Service;
import com.camanagement.capractice.repository.ClientRepository;
import com.camanagement.capractice.repository.ClientServiceRepository;
import com.camanagement.capractice.repository.ServiceRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import com.camanagement.capractice.entity.Invoice;
import com.camanagement.capractice.entity.InvoiceItem;
import com.camanagement.capractice.repository.InvoiceRepository;
import com.camanagement.capractice.repository.InvoiceItemRepository;
import com.camanagement.capractice.entity.Document;
import com.camanagement.capractice.entity.Document.DocumentCategory;
import com.camanagement.capractice.repository.DocumentRepository;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;


import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * COMPLETE DATA LOADER:
 * Creates sample data for both Clients and Services
 */
@Component
public class DataLoader implements CommandLineRunner {

    @Autowired
    private ClientRepository clientRepository;

    @Autowired
    private InvoiceRepository invoiceRepository;

    @Autowired
    private InvoiceItemRepository invoiceItemRepository;

    @Autowired
    private ClientServiceRepository clientServiceRepository;

    @Autowired
    private ServiceRepository serviceRepository;

    @Autowired
    private DocumentRepository documentRepository;

    @Override
    public void run(String... args) throws Exception {

        // Check if data already exists
        if (clientRepository.count() > 0 && serviceRepository.count() > 0 && invoiceRepository.count() > 0 &&
                documentRepository.count() > 0) {
            System.out.println("Data already exists, skipping sample data creation.");
            return;
        }

        System.out.println("Creating sample data...");

        // Create Clients first
        if (clientRepository.count() == 0) {
            createSampleClients();
        }

        // Create Services
        if (serviceRepository.count() == 0) {
            createSampleServices();
        }

        // Create Service Assignments
        if (clientServiceRepository.count() == 0) {
            createSampleServiceAssignments();
        }

        // Create Invoices (NEW!)
        if (invoiceRepository.count() == 0) {
            createSampleInvoices();
        }

        if (documentRepository.count() == 0) {
            createSampleDocuments();
        }

        printStatistics();
    }

    private void createSampleClients() {
        System.out.println("Creating sample clients...");

        // Client 1: Individual Client
        Client client1 = new Client();
        client1.setClientName("Rajesh Kumar");
        client1.setClientType(Client.ClientType.INDIVIDUAL);
        client1.setPanNumber("ABCDE1234F");
        client1.setEmail("rajesh.kumar@email.com");
        client1.setPhone("+91 98765 43210");
        client1.setAddress("123 MG Road, Sector 15");
        client1.setCity("Gurugram");
        client1.setState("Haryana");
        client1.setPincode("122001");
        client1.setStatus(Client.ClientStatus.ACTIVE);
        client1.setRegistrationDate(LocalDate.of(2024, 1, 15));
        client1.setNotes("Long-term client, files ITR annually");

        // Client 2: Company Client
        Client client2 = new Client();
        client2.setClientName("Priya Sharma");
        client2.setCompanyName("Sharma Enterprises Pvt Ltd");
        client2.setClientType(Client.ClientType.COMPANY);
        client2.setPanNumber("FGHIJ5678K");
        client2.setGstin("07FGHIJ5678K1ZY");
        client2.setEmail("priya@sharmaenterprises.com");
        client2.setPhone("+91 87654 32109");
        client2.setAddress("456 Industrial Area, Phase 2");
        client2.setCity("Gurugram");
        client2.setState("Haryana");
        client2.setPincode("122016");
        client2.setStatus(Client.ClientStatus.ACTIVE);
        client2.setRegistrationDate(LocalDate.of(2024, 2, 28));
        client2.setNotes("Manufacturing company, requires audit services");

        // Client 3: Partnership Firm
        Client client3 = new Client();
        client3.setClientName("Amit Patel");
        client3.setCompanyName("Patel & Associates");
        client3.setClientType(Client.ClientType.PARTNERSHIP);
        client3.setPanNumber("KLMNO9876P");
        client3.setGstin("07KLMNO9876P1ZX");
        client3.setEmail("amit@patelassociates.in");
        client3.setPhone("+91 76543 21098");
        client3.setAddress("789 Business District, Golf Course Road");
        client3.setCity("Gurugram");
        client3.setState("Haryana");
        client3.setPincode("122002");
        client3.setStatus(Client.ClientStatus.INACTIVE);
        client3.setRegistrationDate(LocalDate.of(2024, 3, 10));
        client3.setNotes("Partnership firm, seasonal business");

        // Client 4: LLP Client
        Client client4 = new Client();
        client4.setClientName("Sunita Gupta");
        client4.setCompanyName("Tech Solutions LLP");
        client4.setClientType(Client.ClientType.LLP);
        client4.setPanNumber("PQRST1234U");
        client4.setGstin("07PQRST1234U1ZW");
        client4.setEmail("sunita@techsolutions.com");
        client4.setPhone("+91 98123 45678");
        client4.setAddress("101 Cyber City, DLF Phase 3");
        client4.setCity("Gurugram");
        client4.setState("Haryana");
        client4.setPincode("122002");
        client4.setStatus(Client.ClientStatus.ACTIVE);
        client4.setRegistrationDate(LocalDate.of(2024, 1, 5));
        client4.setNotes("IT services company, monthly GST filing required");

        // Client 5: HUF Client
        Client client5 = new Client();
        client5.setClientName("Ramesh Agarwal");
        client5.setCompanyName("Agarwal HUF");
        client5.setClientType(Client.ClientType.HUF);
        client5.setPanNumber("UVWXY5678Z");
        client5.setEmail("ramesh.agarwal@email.com");
        client5.setPhone("+91 91234 56789");
        client5.setAddress("567 Old Gurugram, Near Railway Station");
        client5.setCity("Gurugram");
        client5.setState("Haryana");
        client5.setPincode("122001");
        client5.setStatus(Client.ClientStatus.ACTIVE);
        client5.setRegistrationDate(LocalDate.of(2023, 12, 20));
        client5.setNotes("Traditional family business, property investments");

        // Client 6: Trust
        Client client6 = new Client();
        client6.setClientName("Dr. Meera Verma");
        client6.setCompanyName("Verma Charitable Trust");
        client6.setClientType(Client.ClientType.TRUST);
        client6.setPanNumber("ABCXY9876T");
        client6.setEmail("meera@vermatrust.org");
        client6.setPhone("+91 98765 12345");
        client6.setAddress("234 Charitable Society, Sector 21");
        client6.setCity("Gurugram");
        client6.setState("Haryana");
        client6.setPincode("122015");
        client6.setStatus(Client.ClientStatus.ACTIVE);
        client6.setRegistrationDate(LocalDate.of(2024, 2, 14));
        client6.setNotes("Charitable trust, requires special compliance");

        // Client 7: Recent Individual Client
        Client client7 = new Client();
        client7.setClientName("Vikash Singh");
        client7.setClientType(Client.ClientType.INDIVIDUAL);
        client7.setPanNumber("NEWPA1234N");
        client7.setEmail("vikash.singh@email.com");
        client7.setPhone("+91 87654 98765");
        client7.setAddress("890 New Colony, Sector 45");
        client7.setCity("Gurugram");
        client7.setState("Haryana");
        client7.setPincode("122003");
        client7.setStatus(Client.ClientStatus.ACTIVE);
        client7.setRegistrationDate(LocalDate.now().minusDays(5)); // 5 days ago
        client7.setNotes("New client, first-time filer");

        // Save all clients
        clientRepository.save(client1);
        clientRepository.save(client2);
        clientRepository.save(client3);
        clientRepository.save(client4);
        clientRepository.save(client5);
        clientRepository.save(client6);
        clientRepository.save(client7);

        System.out.println("Sample clients created successfully!");
    }

    private void createSampleServices() {
        System.out.println("Creating sample services...");

        // Service 1: ITR Filing
        Service service1 = new Service();
        service1.setServiceName("Income Tax Return Filing");
        service1.setDescription("Comprehensive income tax return preparation and filing services for individuals and businesses");
        service1.setCategory(Service.ServiceCategory.TAXATION);
        service1.setBasePrice(new BigDecimal("2500.00"));
        service1.setPricingType(Service.PricingType.FIXED);
        service1.setEstimatedDurationDays(3);
        service1.setStatus(Service.ServiceStatus.ACTIVE);
        service1.setRequirements("PAN Card, Form 16, Bank Statements, Investment Proofs");
        service1.setDeliverables("ITR Filed, Acknowledgment Receipt, Tax Computation");
        service1.setNotes("Most popular service for individual clients");

        // Service 2: GST Registration
        Service service2 = new Service();
        service2.setServiceName("GST Registration");
        service2.setDescription("Complete GST registration process including documentation and government liaison");
        service2.setCategory(Service.ServiceCategory.REGISTRATION);
        service2.setBasePrice(new BigDecimal("3000.00"));
        service2.setPricingType(Service.PricingType.FIXED);
        service2.setEstimatedDurationDays(7);
        service2.setStatus(Service.ServiceStatus.ACTIVE);
        service2.setRequirements("PAN Card, Address Proof, Bank Details, Business Registration");
        service2.setDeliverables("GST Certificate, Login Credentials, Compliance Calendar");
        service2.setNotes("High demand service for new businesses");

        // Service 3: Annual Audit
        Service service3 = new Service();
        service3.setServiceName("Annual Audit");
        service3.setDescription("Statutory audit services for companies as per Companies Act requirements");
        service3.setCategory(Service.ServiceCategory.AUDIT_ASSURANCE);
        service3.setBasePrice(new BigDecimal("25000.00"));
        service3.setPricingType(Service.PricingType.FIXED);
        service3.setEstimatedDurationDays(21);
        service3.setStatus(Service.ServiceStatus.ACTIVE);
        service3.setRequirements("Books of Accounts, Bank Statements, Trial Balance, Fixed Asset Register");
        service3.setDeliverables("Audit Report, Management Letter, Compliance Certificate");
        service3.setNotes("Complex service requiring detailed planning");

        // Service 4: Financial Consulting
        Service service4 = new Service();
        service4.setServiceName("Financial Consulting");
        service4.setDescription("Strategic financial advice and business consultation services");
        service4.setCategory(Service.ServiceCategory.ADVISORY);
        service4.setBasePrice(new BigDecimal("5000.00"));
        service4.setPricingType(Service.PricingType.HOURLY);
        service4.setEstimatedDurationDays(null); // Ongoing service
        service4.setStatus(Service.ServiceStatus.ACTIVE);
        service4.setRequirements("Financial Statements, Business Plan, Cash Flow Projections");
        service4.setDeliverables("Financial Analysis Report, Recommendations, Action Plan");
        service4.setNotes("Premium consulting service");

        // Service 5: Bookkeeping
        Service service5 = new Service();
        service5.setServiceName("Monthly Bookkeeping");
        service5.setDescription("Complete bookkeeping and accounting services on monthly basis");
        service5.setCategory(Service.ServiceCategory.ACCOUNTING);
        service5.setBasePrice(new BigDecimal("8000.00"));
        service5.setPricingType(Service.PricingType.MONTHLY);
        service5.setEstimatedDurationDays(5);
        service5.setStatus(Service.ServiceStatus.ACTIVE);
        service5.setRequirements("Invoices, Bills, Bank Statements, Previous Month Books");
        service5.setDeliverables("Monthly Books, Trial Balance, P&L Statement, Balance Sheet");
        service5.setNotes("Recurring monthly service");

        // Service 6: Company Formation
        Service service6 = new Service();
        service6.setServiceName("Private Limited Company Formation");
        service6.setDescription("Complete company incorporation process with ROC filing");
        service6.setCategory(Service.ServiceCategory.REGISTRATION);
        service6.setBasePrice(new BigDecimal("15000.00"));
        service6.setPricingType(Service.PricingType.FIXED);
        service6.setEstimatedDurationDays(14);
        service6.setStatus(Service.ServiceStatus.ACTIVE);
        service6.setRequirements("Director Details, Address Proof, DIN, DSC, MOA/AOA");
        service6.setDeliverables("Certificate of Incorporation, PAN, TAN, Bank Account Opening Letter");
        service6.setNotes("Complete business setup service");

        // Save all services
        serviceRepository.save(service1);
        serviceRepository.save(service2);
        serviceRepository.save(service3);
        serviceRepository.save(service4);
        serviceRepository.save(service5);
        serviceRepository.save(service6);

        System.out.println("Sample services created successfully!");
    }

    private void printStatistics() {
        long totalClients = clientRepository.count();
        long totalServices = serviceRepository.count();
        long activeClients = clientRepository.countByStatus(Client.ClientStatus.ACTIVE);
        long activeServices = serviceRepository.countByStatus(Service.ServiceStatus.ACTIVE);
        long totalInvoices = invoiceRepository.count();
        long paidInvoices = invoiceRepository.countByStatus(Invoice.InvoiceStatus.PAID);
        long totalDocuments = documentRepository.count();



        System.out.println("=== SAMPLE DATA STATISTICS ===");
        System.out.println("Total clients: " + totalClients);
        System.out.println("Active clients: " + activeClients);
        System.out.println("Total services: " + totalServices);
        System.out.println("Active services: " + activeServices);
        System.out.println("Total invoices: " + totalInvoices);
        System.out.println("Paid invoices: " + paidInvoices);
        System.out.println("Total documents: " + totalDocuments);
        System.out.println("================================");
    }

    private void createSampleServiceAssignments() {
        System.out.println("Creating sample service assignments...");

        // Get all clients and services
        List<Client> clients = clientRepository.findAll();
        List<Service> services = serviceRepository.findAll();

        if (clients.isEmpty() || services.isEmpty()) {
            System.out.println("No clients or services found. Skipping assignments.");
            return;
        }

        // Assignment 1: ITR Filing for Rajesh Kumar (overdue)
        Client rajesh = clients.stream().filter(c -> "Rajesh Kumar".equals(c.getClientName())).findFirst().orElse(clients.get(0));
        Service itrService = services.stream().filter(s -> s.getServiceName().contains("Income Tax")).findFirst().orElse(services.get(0));

        ClientService assignment1 = new ClientService();
        assignment1.setClient(rajesh);
        assignment1.setService(itrService);
        assignment1.setAssignedDate(LocalDate.of(2024, 2, 15));
        assignment1.setDueDate(LocalDate.of(2024, 3, 31)); // Past due date (overdue)
        assignment1.setStatus(ClientService.ServiceStatus.IN_PROGRESS);
        assignment1.setQuotedPrice(new BigDecimal("2500.00"));
        assignment1.setNotes("ITR for FY 2023-24. Client provided all documents.");
        clientServiceRepository.save(assignment1);

        // Assignment 2: GST Registration for Priya Sharma (upcoming deadline)
        Client priya = clients.stream().filter(c -> "Priya Sharma".equals(c.getClientName())).findFirst().orElse(clients.get(1));
        Service gstService = services.stream().filter(s -> s.getServiceName().contains("GST")).findFirst().orElse(services.get(1));

        ClientService assignment2 = new ClientService();
        assignment2.setClient(priya);
        assignment2.setService(gstService);
        assignment2.setAssignedDate(LocalDate.now().minusDays(3));
        assignment2.setDueDate(LocalDate.now().plusDays(5)); // Due in 5 days (upcoming)
        assignment2.setStatus(ClientService.ServiceStatus.ASSIGNED);
        assignment2.setQuotedPrice(new BigDecimal("3000.00"));
        assignment2.setNotes("New business registration. Priority client.");
        clientServiceRepository.save(assignment2);

        // Assignment 3: Annual Audit for Patel & Associates (in progress)
        Client patel = clients.stream().filter(c -> c.getClientName().contains("Patel")).findFirst().orElse(clients.get(2));
        Service auditService = services.stream().filter(s -> s.getServiceName().contains("Audit")).findFirst().orElse(services.get(2));

        if (patel != null && auditService != null) {
            ClientService assignment3 = new ClientService();
            assignment3.setClient(patel);
            assignment3.setService(auditService);
            assignment3.setAssignedDate(LocalDate.now().minusDays(10));
            assignment3.setDueDate(LocalDate.now().plusDays(15));
            assignment3.setStatus(ClientService.ServiceStatus.IN_PROGRESS);
            assignment3.setQuotedPrice(new BigDecimal("28000.00"));
            assignment3.setNotes("Annual audit for FY 2023-24. Complex case with multiple subsidiaries.");
            clientServiceRepository.save(assignment3);
        }

        // Assignment 4: Financial Consulting for Tech Solutions LLP (completed)
        Client techSolutions = clients.stream().filter(c -> c.getClientName().contains("Sunita")).findFirst().orElse(clients.get(3));
        Service consultingService = services.stream().filter(s -> s.getServiceName().contains("Consulting")).findFirst().orElse(services.get(3));

        if (techSolutions != null && consultingService != null) {
            ClientService assignment4 = new ClientService();
            assignment4.setClient(techSolutions);
            assignment4.setService(consultingService);
            assignment4.setAssignedDate(LocalDate.of(2024, 1, 15));
            assignment4.setDueDate(LocalDate.of(2024, 2, 15));
            assignment4.setCompletionDate(LocalDate.of(2024, 2, 10)); // Completed early
            assignment4.setStatus(ClientService.ServiceStatus.COMPLETED);
            assignment4.setQuotedPrice(new BigDecimal("15000.00"));
            assignment4.setNotes("Financial restructuring consultation completed successfully.");
            clientServiceRepository.save(assignment4);
        }

        // Assignment 5: Monthly Bookkeeping for Agarwal HUF (active)
        Client agarwal = clients.stream().filter(c -> c.getClientName().contains("Ramesh")).findFirst().orElse(clients.get(4));
        Service bookkeepingService = services.stream().filter(s -> s.getServiceName().contains("Bookkeeping")).findFirst().orElse(services.get(4));

        if (agarwal != null && bookkeepingService != null) {
            ClientService assignment5 = new ClientService();
            assignment5.setClient(agarwal);
            assignment5.setService(bookkeepingService);
            assignment5.setAssignedDate(LocalDate.now().minusDays(30));
            assignment5.setDueDate(LocalDate.now().plusDays(2)); // Due soon
            assignment5.setStatus(ClientService.ServiceStatus.IN_PROGRESS);
            assignment5.setQuotedPrice(new BigDecimal("8000.00"));
            assignment5.setNotes("Monthly bookkeeping for March 2024.");
            clientServiceRepository.save(assignment5);
        }

        // Assignment 6: Company Formation for Dr. Meera Verma (assigned)
        Client meera = clients.stream().filter(c -> c.getClientName().contains("Meera")).findFirst().orElse(clients.get(5));
        Service companyFormationService = services.stream().filter(s -> s.getServiceName().contains("Company")).findFirst().orElse(services.get(5));

        if (meera != null && companyFormationService != null) {
            ClientService assignment6 = new ClientService();
            assignment6.setClient(meera);
            assignment6.setService(companyFormationService);
            assignment6.setAssignedDate(LocalDate.now().minusDays(2));
            assignment6.setDueDate(LocalDate.now().plusDays(12));
            assignment6.setStatus(ClientService.ServiceStatus.ASSIGNED);
            assignment6.setQuotedPrice(new BigDecimal("15000.00"));
            assignment6.setNotes("Setting up a subsidiary for the trust. Need DIN and DSC first.");
            clientServiceRepository.save(assignment6);
        }

        System.out.println("Sample service assignments created successfully!");
    }

    private void createSampleInvoices() {
        System.out.println("Creating sample invoices...");

        // Get clients and services
        List<Client> clients = clientRepository.findAll();
        List<ClientService> allAssignments = clientServiceRepository.findAll();

        if (clients.isEmpty() || allAssignments.isEmpty()) {
            System.out.println("No clients or service assignments found. Skipping invoices.");
            return;
        }

        // Invoice 1: PAID Invoice for Rajesh Kumar
        Client rajesh = clients.stream()
                .filter(c -> "Rajesh Kumar".equals(c.getClientName()))
                .findFirst()
                .orElse(clients.get(0));

        Invoice invoice1 = new Invoice();
        invoice1.setClient(rajesh);
        invoice1.setInvoiceNumber("INV-2024-0001");
        invoice1.setInvoiceDate(LocalDate.of(2024, 1, 15));
        invoice1.setDueDate(LocalDate.of(2024, 1, 30));
        invoice1.setTaxPercentage(new BigDecimal("18.00"));
        invoice1.setDiscountPercentage(BigDecimal.ZERO);
        invoice1.setStatus(Invoice.InvoiceStatus.PAID);
        invoice1.setPaymentMethod(Invoice.PaymentMethod.BANK_TRANSFER);
        invoice1.setPaymentReference("TXN123456789");
        invoice1.setPaymentDate(LocalDate.of(2024, 1, 28));

        // Add items to invoice 1
        InvoiceItem item1 = new InvoiceItem();
        item1.setDescription("Income Tax Return Filing - FY 2023-24");
        item1.setQuantity(BigDecimal.ONE);
        item1.setUnitPrice(new BigDecimal("2500.00"));
        item1.setItemOrder(1);
        item1.calculateAmount();
        invoice1.addItem(item1);

        InvoiceItem item2 = new InvoiceItem();
        item2.setDescription("GST Consultation");
        item2.setQuantity(BigDecimal.ONE);
        item2.setUnitPrice(new BigDecimal("1500.00"));
        item2.setItemOrder(2);
        item2.calculateAmount();
        invoice1.addItem(item2);

        invoice1.calculateTotals();
        invoice1.setPaidAmount(invoice1.getTotalAmount()); // Fully paid
        invoiceRepository.save(invoice1);

        // Invoice 2: SENT Invoice for Priya Sharma
        Client priya = clients.stream()
                .filter(c -> "Priya Sharma".equals(c.getClientName()))
                .findFirst()
                .orElse(clients.get(1));

        Invoice invoice2 = new Invoice();
        invoice2.setClient(priya);
        invoice2.setInvoiceNumber("INV-2024-0002");
        invoice2.setInvoiceDate(LocalDate.of(2024, 2, 1));
        invoice2.setDueDate(LocalDate.of(2024, 2, 16));
        invoice2.setTaxPercentage(new BigDecimal("18.00"));
        invoice2.setDiscountPercentage(new BigDecimal("5.00")); // 5% discount
        invoice2.setStatus(Invoice.InvoiceStatus.SENT);

        InvoiceItem item3 = new InvoiceItem();
        item3.setDescription("GST Registration");
        item3.setQuantity(BigDecimal.ONE);
        item3.setUnitPrice(new BigDecimal("3000.00"));
        item3.setItemOrder(1);
        item3.calculateAmount();
        invoice2.addItem(item3);

        invoice2.calculateTotals();
        invoiceRepository.save(invoice2);

        // Invoice 3: OVERDUE Invoice for Amit Patel
        Client patel = clients.stream()
                .filter(c -> c.getClientName().contains("Patel"))
                .findFirst()
                .orElse(clients.get(2));

        Invoice invoice3 = new Invoice();
        invoice3.setClient(patel);
        invoice3.setInvoiceNumber("INV-2024-0003");
        invoice3.setInvoiceDate(LocalDate.of(2024, 1, 10));
        invoice3.setDueDate(LocalDate.of(2024, 1, 25)); // Past due date - OVERDUE
        invoice3.setTaxPercentage(new BigDecimal("18.00"));
        invoice3.setDiscountPercentage(BigDecimal.ZERO);
        invoice3.setStatus(Invoice.InvoiceStatus.OVERDUE);

        InvoiceItem item4 = new InvoiceItem();
        item4.setDescription("Annual Audit Services");
        item4.setQuantity(BigDecimal.ONE);
        item4.setUnitPrice(new BigDecimal("25000.00"));
        item4.setItemOrder(1);
        item4.calculateAmount();
        invoice3.addItem(item4);

        invoice3.calculateTotals();
        invoiceRepository.save(invoice3);

        // Invoice 4: PARTIALLY PAID Invoice for Sunita Gupta
        Client sunita = clients.stream()
                .filter(c -> c.getClientName().contains("Sunita"))
                .findFirst()
                .orElse(clients.get(3));

        Invoice invoice4 = new Invoice();
        invoice4.setClient(sunita);
        invoice4.setInvoiceNumber("INV-2024-0004");
        invoice4.setInvoiceDate(LocalDate.of(2024, 2, 10));
        invoice4.setDueDate(LocalDate.of(2024, 2, 25));
        invoice4.setTaxPercentage(new BigDecimal("18.00"));
        invoice4.setDiscountPercentage(BigDecimal.ZERO);
        invoice4.setStatus(Invoice.InvoiceStatus.PARTIALLY_PAID);
        invoice4.setPaymentMethod(Invoice.PaymentMethod.UPI);
        invoice4.setPaymentReference("UPI/234567890");
        invoice4.setPaymentDate(LocalDate.of(2024, 2, 15));

        InvoiceItem item5 = new InvoiceItem();
        item5.setDescription("Financial Consulting - 10 Hours");
        item5.setQuantity(new BigDecimal("10"));
        item5.setUnitPrice(new BigDecimal("500.00"));
        item5.setItemOrder(1);
        item5.calculateAmount();
        invoice4.addItem(item5);

        InvoiceItem item6 = new InvoiceItem();
        item6.setDescription("Monthly Bookkeeping");
        item6.setQuantity(BigDecimal.ONE);
        item6.setUnitPrice(new BigDecimal("8000.00"));
        item6.setItemOrder(2);
        item6.calculateAmount();
        invoice4.addItem(item6);

        invoice4.calculateTotals();
        // Partially paid - 50% paid
        invoice4.setPaidAmount(invoice4.getTotalAmount().divide(new BigDecimal("2")));
        invoiceRepository.save(invoice4);

        // Invoice 5: DRAFT Invoice for Ramesh Agarwal
        Client ramesh = clients.stream()
                .filter(c -> c.getClientName().contains("Ramesh"))
                .findFirst()
                .orElse(clients.get(4));

        Invoice invoice5 = new Invoice();
        invoice5.setClient(ramesh);
        invoice5.setInvoiceNumber("INV-2024-0005");
        invoice5.setInvoiceDate(LocalDate.now());
        invoice5.setDueDate(LocalDate.now().plusDays(15));
        invoice5.setTaxPercentage(new BigDecimal("18.00"));
        invoice5.setDiscountPercentage(BigDecimal.ZERO);
        invoice5.setStatus(Invoice.InvoiceStatus.DRAFT);

        InvoiceItem item7 = new InvoiceItem();
        item7.setDescription("Monthly Bookkeeping - March 2024");
        item7.setQuantity(BigDecimal.ONE);
        item7.setUnitPrice(new BigDecimal("8000.00"));
        item7.setItemOrder(1);
        item7.calculateAmount();
        invoice5.addItem(item7);

        invoice5.calculateTotals();
        invoiceRepository.save(invoice5);

        System.out.println("Sample invoices created successfully!");
    }

    private void createSampleDocuments() {
        System.out.println("Creating sample documents...");

        // Get clients
        List<Client> clients = clientRepository.findAll();
        if (clients.isEmpty()) {
            System.out.println("No clients found. Skipping documents.");
            return;
        }

        // Create upload directory if it doesn't exist
        try {
            Path uploadPath = Paths.get("uploads/documents/2024/11");
            Files.createDirectories(uploadPath);
            System.out.println("Created upload directory: " + uploadPath);
        } catch (Exception e) {
            System.err.println("Error creating upload directory: " + e.getMessage());
        }
        // Document 1: Tax document for Rajesh Kumar
        Client rajesh = clients.stream()
                .filter(c -> "Rajesh Kumar".equals(c.getClientName()))
                .findFirst()
                .orElse(clients.get(0));

        Document doc1 = getDocument(rajesh);
        documentRepository.save(doc1);

        // Document 2: Bank statement for Rajesh
        Document doc2 = new Document();
        doc2.setFileName("Bank_Statement_Jan2024.pdf");
        doc2.setFilePath("uploads/documents/2024/11/bank_statement_rajesh.pdf");
        doc2.setFileSize(1048576L); // 1 MB
        doc2.setFileType("application/pdf");
        doc2.setFileExtension("pdf");
        doc2.setClient(rajesh);
        doc2.setCategory(DocumentCategory.FINANCIAL);
        doc2.setDescription("HDFC Bank statement for January 2024");
        doc2.setFinancialYear("2023-24");
        doc2.setUploadedBy("Admin");
        doc2.setUploadDate(LocalDateTime.of(2024, 2, 1, 14, 20));
        doc2.setTags("bank statement, hdfc, january");
        doc2.setIsDeleted(false);
        documentRepository.save(doc2);

        // Document 3: GST certificate for Priya Sharma
        Client priya = clients.stream()
                .filter(c -> "Priya Sharma".equals(c.getClientName()))
                .findFirst()
                .orElse(clients.get(1));

        Document doc3 = new Document();
        doc3.setFileName("GST_Registration_Certificate.pdf");
        doc3.setFilePath("uploads/documents/2024/11/gst_cert_priya.pdf");
        doc3.setFileSize(204800L); // 200 KB
        doc3.setFileType("application/pdf");
        doc3.setFileExtension("pdf");
        doc3.setClient(priya);
        doc3.setCategory(DocumentCategory.COMPLIANCE);
        doc3.setDescription("GST Registration Certificate for Sharma Enterprises");
        doc3.setFinancialYear("2023-24");
        doc3.setUploadedBy("Admin");
        doc3.setUploadDate(LocalDateTime.of(2024, 2, 28, 11, 45));
        doc3.setTags("gst, registration, certificate, compliance");
        doc3.setIsDeleted(false);
        documentRepository.save(doc3);

        // Document 4: Audit report for Patel
        Client patel = clients.stream()
                .filter(c -> c.getClientName().contains("Patel"))
                .findFirst()
                .orElse(clients.get(2));

        if (patel != null) {
            Document doc4 = new Document();
            doc4.setFileName("Annual_Audit_Report_2023.pdf");
            doc4.setFilePath("uploads/documents/2024/11/audit_report_patel.pdf");
            doc4.setFileSize(2097152L); // 2 MB
            doc4.setFileType("application/pdf");
            doc4.setFileExtension("pdf");
            doc4.setClient(patel);
            doc4.setCategory(DocumentCategory.AUDIT);
            doc4.setDescription("Annual audit report for FY 2022-23");
            doc4.setFinancialYear("2022-23");
            doc4.setUploadedBy("Admin");
            doc4.setUploadDate(LocalDateTime.of(2024, 3, 10, 16, 0));
            doc4.setTags("audit, annual report, 2023");
            doc4.setIsDeleted(false);
            documentRepository.save(doc4);
        }

        // Document 5: Excel file for Sunita
        Client sunita = clients.stream()
                .filter(c -> c.getClientName().contains("Sunita"))
                .findFirst()
                .orElse(clients.get(3));

        if (sunita != null) {
            Document doc5 = new Document();
            doc5.setFileName("Financial_Statement_Q1_2024.xlsx");
            doc5.setFilePath("uploads/documents/2024/11/financial_sunita.xlsx");
            doc5.setFileSize(819200L); // 800 KB
            doc5.setFileType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            doc5.setFileExtension("xlsx");
            doc5.setClient(sunita);
            doc5.setCategory(DocumentCategory.FINANCIAL);
            doc5.setDescription("Quarterly financial statement Q1 2024");
            doc5.setFinancialYear("2024-25");
            doc5.setUploadedBy("Admin");
            doc5.setUploadDate(LocalDateTime.of(2024, 4, 5, 9, 15));
            doc5.setTags("financial statement, quarterly, excel");
            doc5.setIsDeleted(false);
            documentRepository.save(doc5);
        }

        // Document 6: PAN card copy for Ramesh
        Client ramesh = clients.stream()
                .filter(c -> c.getClientName().contains("Ramesh"))
                .findFirst()
                .orElse(clients.get(4));

        if (ramesh != null) {
            Document doc6 = new Document();
            doc6.setFileName("PAN_Card_Copy.jpg");
            doc6.setFilePath("uploads/documents/2024/11/pan_ramesh.jpg");
            doc6.setFileSize(153600L); // 150 KB
            doc6.setFileType("image/jpeg");
            doc6.setFileExtension("jpg");
            doc6.setClient(ramesh);
            doc6.setCategory(DocumentCategory.IDENTITY);
            doc6.setDescription("Scanned copy of PAN card");
            doc6.setUploadedBy("Admin");
            doc6.setUploadDate(LocalDateTime.now().minusDays(10));
            doc6.setTags("pan card, identity, kyc");
            doc6.setIsDeleted(false);
            documentRepository.save(doc6);
        }

        System.out.println("Sample documents created successfully!");
    }

    private static Document getDocument(Client rajesh) {
        Document doc1 = new Document();
        doc1.setFileName("Form16_FY2023-24.pdf");
        doc1.setFilePath("uploads/documents/2024/11/form16_rajesh.pdf");
        doc1.setFileSize(524288L); // 512 KB
        doc1.setFileType("application/pdf");
        doc1.setFileExtension("pdf");
        doc1.setClient(rajesh);
        doc1.setCategory(DocumentCategory.TAX);
        doc1.setDescription("Form 16 for FY 2023-24, received from employer");
        doc1.setFinancialYear("2023-24");
        doc1.setUploadedBy("Admin");
        doc1.setUploadDate(LocalDateTime.of(2024, 1, 15, 10, 30));
        doc1.setTags("income tax, salary, form16, 2023-24");
        doc1.setIsDeleted(false);
        return doc1;
    }


}
