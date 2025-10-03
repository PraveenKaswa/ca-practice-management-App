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


import java.math.BigDecimal;
import java.time.LocalDate;
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
    private ClientServiceRepository clientServiceRepository;

    @Autowired
    private ServiceRepository serviceRepository;

    @Override
    public void run(String... args) throws Exception {

        // Check if data already exists
        if (clientRepository.count() > 0 && serviceRepository.count() > 0) {
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

        System.out.println("=== SAMPLE DATA STATISTICS ===");
        System.out.println("Total clients: " + totalClients);
        System.out.println("Active clients: " + activeClients);
        System.out.println("Total services: " + totalServices);
        System.out.println("Active services: " + activeServices);
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
}