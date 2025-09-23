package com.camanagement.capractice.controller;

import com.camanagement.capractice.entity.Client;
import com.camanagement.capractice.entity.Service;
import com.camanagement.capractice.repository.ClientRepository;
import com.camanagement.capractice.repository.ServiceRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import com.camanagement.capractice.entity.ClientService;
import com.camanagement.capractice.repository.ClientServiceRepository;


import java.time.LocalDate;
import java.util.List;

@Controller
public class DashboardController {

    @Autowired
    private ClientRepository clientRepository;

    @Autowired
    private ServiceRepository serviceRepository;

    @GetMapping("/")
    public String dashboard(Model model) {
        try {
            System.out.println("=== LOADING DASHBOARD ===");

            // CLIENT STATISTICS
            long totalClients = clientRepository.countByStatus(Client.ClientStatus.ACTIVE);
            long totalServices = serviceRepository.countByStatus(Service.ServiceStatus.ACTIVE);

            // SERVICE ASSIGNMENT STATISTICS
            long totalAssignments = clientServiceRepository.count();
            long activeAssignments = clientServiceRepository.countByStatus(ClientService.ServiceStatus.IN_PROGRESS) +
                    clientServiceRepository.countByStatus(ClientService.ServiceStatus.ASSIGNED);

            // OVERDUE TASKS (Real data!)
            LocalDate today = LocalDate.now();
            long overdueTasks = clientServiceRepository.countByDueDateBefore(today);

            // UPCOMING DEADLINES (Next 7 days)
            LocalDate nextWeek = today.plusDays(7);
            List<ClientService> upcomingServices = clientServiceRepository.findUpcomingServices(today, nextWeek);
            long upcomingDeadlines = upcomingServices.size();

            // RECENT ACTIVITIES
            List<ClientService> recentAssignments = clientServiceRepository.findRecentlyCreated();
            // Limit to 5 most recent
            if (recentAssignments.size() > 5) {
                recentAssignments = recentAssignments.subList(0, 5);
            }

            // COMPLETED THIS MONTH
            LocalDate startOfMonth = LocalDate.now().withDayOfMonth(1);
            long completedThisMonth = clientServiceRepository.countCompletedServicesBetween(startOfMonth, today);

            System.out.println("Dashboard stats calculated:");
            System.out.println("- Total clients: " + totalClients);
            System.out.println("- Active assignments: " + activeAssignments);
            System.out.println("- Overdue tasks: " + overdueTasks);
            System.out.println("- Upcoming deadlines: " + upcomingDeadlines);

            // ADD TO MODEL
            model.addAttribute("welcomeMessage", "Welcome to CA Practice Management System");
            model.addAttribute("totalClients", totalClients);
            model.addAttribute("totalServices", totalServices);
            model.addAttribute("totalAssignments", totalAssignments);
            model.addAttribute("activeAssignments", activeAssignments);
            model.addAttribute("overdueTasks", overdueTasks);
            model.addAttribute("upcomingDeadlines", upcomingDeadlines);
            model.addAttribute("completedThisMonth", completedThisMonth);
            model.addAttribute("recentAssignments", recentAssignments);

            return "dashboard";

        } catch (Exception e) {
            System.err.println("ERROR in dashboard method: " + e.getMessage());
            e.printStackTrace();

            // Fallback with static data
            model.addAttribute("welcomeMessage", "Welcome (Fallback Mode)");
            model.addAttribute("totalClients", 0);
            model.addAttribute("totalServices", 0);
            model.addAttribute("activeAssignments", 0);
            model.addAttribute("overdueTasks", 0);
            model.addAttribute("upcomingDeadlines", 0);

            return "dashboard";
        }
    }

    @GetMapping("/dashboard")
    public String dashboardAlternate(Model model) {
        return dashboard(model);
    }

    @GetMapping("/about")
    public String about(Model model) {
        model.addAttribute("systemName", "CA Practice Management System");
        model.addAttribute("version", "1.0.0");
        model.addAttribute("developer", "Praveen");
        model.addAttribute("description", "Comprehensive practice management solution for CA firms");

        return "about";
    }

    @GetMapping("/clients")
    public String clients(
            @RequestParam(value = "search", required = false) String searchTerm,
            @RequestParam(value = "type", required = false) String clientType,
            @RequestParam(value = "status", required = false) String clientStatus,
            Model model) {

        /**
         * ENHANCED CLIENTS LIST WITH SEARCH:
         *
         * @RequestParam - Captures URL parameters like ?search=john&type=individual
         * required = false - Parameters are optional
         *
         * Examples:
         * /clients                           -> Show all clients
         * /clients?search=john              -> Search for "john"
         * /clients?type=INDIVIDUAL          -> Filter by type
         * /clients?search=john&type=COMPANY -> Search + filter
         */

        try {
            List<Client> allClients;

            System.out.println("=== CLIENT SEARCH ===");
            System.out.println("Search term: " + searchTerm);
            System.out.println("Client type filter: " + clientType);
            System.out.println("Client status filter: " + clientStatus);

            // Check if any search/filter parameters are provided
            boolean hasSearch = searchTerm != null && !searchTerm.trim().isEmpty();
            boolean hasTypeFilter = clientType != null && !clientType.trim().isEmpty();
            boolean hasStatusFilter = clientStatus != null && !clientStatus.trim().isEmpty();

            if (hasSearch || hasTypeFilter || hasStatusFilter) {
                // FILTERED/SEARCHED RESULTS
                allClients = performClientSearch(searchTerm, clientType, clientStatus);
                System.out.println("Filtered results: " + allClients.size() + " clients");
            } else {
                // NO FILTERS - SHOW ALL CLIENTS
                allClients = clientRepository.findAllByOrderByClientNameAsc();
                System.out.println("Showing all clients: " + allClients.size());
            }

            // Get statistics (for the whole database, not just filtered results)
            long totalClients = clientRepository.count();
            long activeClients = clientRepository.countByStatus(Client.ClientStatus.ACTIVE);
            long inactiveClients = clientRepository.countByStatus(Client.ClientStatus.INACTIVE);

            // Add data to model
            model.addAttribute("clients", allClients);
            model.addAttribute("totalClients", totalClients);
            model.addAttribute("activeClients", activeClients);
            model.addAttribute("inactiveClients", inactiveClients);

            // Keep search parameters in model so form stays filled
            model.addAttribute("searchTerm", searchTerm);
            model.addAttribute("selectedType", clientType);
            model.addAttribute("selectedStatus", clientStatus);

            // Add result count for user feedback
            model.addAttribute("resultCount", allClients.size());
            model.addAttribute("isFiltered", hasSearch || hasTypeFilter || hasStatusFilter);

            return "clients";

        } catch (Exception e) {
            System.err.println("ERROR in clients search: " + e.getMessage());
            e.printStackTrace();

            // Fallback to showing all clients
            model.addAttribute("clients", clientRepository.findAllByOrderByClientNameAsc());
            model.addAttribute("totalClients", 0);
            model.addAttribute("activeClients", 0);
            model.addAttribute("inactiveClients", 0);
            return "clients";
        }
    }

    /**
     * HELPER METHOD: Perform the actual search based on parameters
     */
    private List<Client> performClientSearch(String searchTerm, String clientType, String clientStatus) {

        // Start with all clients
        List<Client> results = clientRepository.findAllByOrderByClientNameAsc();

        // Apply search term filter
        if (searchTerm != null && !searchTerm.trim().isEmpty()) {
            String search = searchTerm.trim().toLowerCase();
            results = results.stream()
                    .filter(client ->
                            (client.getClientName() != null && client.getClientName().toLowerCase().contains(search)) ||
                                    (client.getCompanyName() != null && client.getCompanyName().toLowerCase().contains(search)) ||
                                    (client.getPanNumber() != null && client.getPanNumber().toLowerCase().contains(search)) ||
                                    (client.getEmail() != null && client.getEmail().toLowerCase().contains(search))
                    )
                    .collect(java.util.stream.Collectors.toList());
        }

        // Apply client type filter
        if (clientType != null && !clientType.trim().isEmpty() && !clientType.equals("ALL")) {
            try {
                Client.ClientType typeEnum = Client.ClientType.valueOf(clientType);
                results = results.stream()
                        .filter(client -> client.getClientType() == typeEnum)
                        .collect(java.util.stream.Collectors.toList());
            } catch (IllegalArgumentException e) {
                System.err.println("Invalid client type: " + clientType);
            }
        }

        // Apply status filter
        if (clientStatus != null && !clientStatus.trim().isEmpty() && !clientStatus.equals("ALL")) {
            try {
                Client.ClientStatus statusEnum = Client.ClientStatus.valueOf(clientStatus);
                results = results.stream()
                        .filter(client -> client.getStatus() == statusEnum)
                        .collect(java.util.stream.Collectors.toList());
            } catch (IllegalArgumentException e) {
                System.err.println("Invalid client status: " + clientStatus);
            }
        }

        return results;
    }

    @GetMapping("/services")
    public String services(Model model) {
        try {
            /**
             * SERVICES PAGE WITH REAL DATA:
             * Get all services from database and organize by category
             */

            // Get all active services ordered by name
            List<Service> allServices = serviceRepository.findAllActiveServices();

            // Get services by category for better organization
            List<Service> taxationServices = serviceRepository.findActiveServicesByCategory(Service.ServiceCategory.TAXATION);
            List<Service> auditServices = serviceRepository.findActiveServicesByCategory(Service.ServiceCategory.AUDIT_ASSURANCE);
            List<Service> complianceServices = serviceRepository.findActiveServicesByCategory(Service.ServiceCategory.COMPLIANCE);
            List<Service> registrationServices = serviceRepository.findActiveServicesByCategory(Service.ServiceCategory.REGISTRATION);
            List<Service> accountingServices = serviceRepository.findActiveServicesByCategory(Service.ServiceCategory.ACCOUNTING);
            List<Service> advisoryServices = serviceRepository.findActiveServicesByCategory(Service.ServiceCategory.ADVISORY);

            // Get service statistics
            long totalServices = serviceRepository.count();
            long activeServices = serviceRepository.countByStatus(Service.ServiceStatus.ACTIVE);
            long inactiveServices = serviceRepository.countByStatus(Service.ServiceStatus.INACTIVE);

            // Count services by category
            long taxationCount = serviceRepository.countByCategory(Service.ServiceCategory.TAXATION);
            long auditCount = serviceRepository.countByCategory(Service.ServiceCategory.AUDIT_ASSURANCE);
            long complianceCount = serviceRepository.countByCategory(Service.ServiceCategory.COMPLIANCE);

            // Add all data to model
            model.addAttribute("allServices", allServices);
            model.addAttribute("taxationServices", taxationServices);
            model.addAttribute("auditServices", auditServices);
            model.addAttribute("complianceServices", complianceServices);
            model.addAttribute("registrationServices", registrationServices);
            model.addAttribute("accountingServices", accountingServices);
            model.addAttribute("advisoryServices", advisoryServices);

            // Statistics
            model.addAttribute("totalServices", totalServices);
            model.addAttribute("activeServices", activeServices);
            model.addAttribute("inactiveServices", inactiveServices);
            model.addAttribute("taxationCount", taxationCount);
            model.addAttribute("auditCount", auditCount);
            model.addAttribute("complianceCount", complianceCount);

            return "services";

        } catch (Exception e) {
            System.err.println("ERROR in services method: " + e.getMessage());
            e.printStackTrace();

            // Fallback with empty data
            model.addAttribute("allServices", List.of());
            model.addAttribute("totalServices", 0);
            model.addAttribute("activeServices", 0);
            return "services";
        }
    }

    @GetMapping("/documents")
    public String documents(Model model) {
        model.addAttribute("pageTitle", "Document Management");
        // TODO: Add real document data when Document entity is created
        return "documents";
    }

    @GetMapping("/reports")
    public String reports(Model model) {
        try {
            // Get client statistics
            long totalClients = clientRepository.count();
            long individualClients = clientRepository.countByClientType(Client.ClientType.INDIVIDUAL);
            long companyClients = clientRepository.countByClientType(Client.ClientType.COMPANY);

            // Get service statistics
            long totalServices = serviceRepository.count();
            long activeServices = serviceRepository.countByStatus(Service.ServiceStatus.ACTIVE);

            // Calculate this month's new clients
            LocalDate startOfMonth = LocalDate.now().withDayOfMonth(1);
            long newClientsThisMonth = clientRepository.countByRegistrationDateAfter(startOfMonth);

            // Add to model
            model.addAttribute("totalClients", totalClients);
            model.addAttribute("individualClients", individualClients);
            model.addAttribute("companyClients", companyClients);
            model.addAttribute("totalServices", totalServices);
            model.addAttribute("activeServices", activeServices);
            model.addAttribute("newClientsThisMonth", newClientsThisMonth);

            // Simulated data for other stats (replace with real entities later)
            model.addAttribute("monthlyRevenue", "₹12.5L");
            model.addAttribute("tasksCompleted", "89%");
            model.addAttribute("reportsGenerated", 23);

            return "reports";

        } catch (Exception e) {
            System.err.println("ERROR in reports method: " + e.getMessage());
            // Fallback data
            model.addAttribute("totalClients", 0);
            model.addAttribute("totalServices", 0);
            model.addAttribute("monthlyRevenue", "₹0");
            return "reports";
        }

    }
    @GetMapping("/clients/new")
    public String showAddClientForm(Model model) {
        /**
         * SHOW ADD CLIENT FORM:
         *
         * WHY: User clicks "Add New Client" button, needs to see the form
         * WHAT: Create empty Client object and pass to template
         * HOW: Thymeleaf binds form fields to this object
         */

        try {
            // Create new empty client object for form binding
            Client newClient = new Client();

            // Set some defaults
            newClient.setStatus(Client.ClientStatus.ACTIVE);
            newClient.setRegistrationDate(LocalDate.now());

            // Pass to template for form binding
            model.addAttribute("client", newClient);
            model.addAttribute("isEdit", false);

            System.out.println("Showing add client form");
            return "add-client"; // Returns add-client.html template

        } catch (Exception e) {
            System.err.println("ERROR showing add client form: " + e.getMessage());
            return "redirect:/clients"; // Redirect back to clients list if error
        }
    }

    @PostMapping("/clients/add")
    public String addClient(@ModelAttribute("client") Client client, Model model) {
        /**
         * PROCESS ADD CLIENT FORM:
         *
         * WHY: User filled form and clicked "Add Client" - need to save to database
         * WHAT: Take form data, validate it, save to database, redirect to success
         * HOW: Spring automatically converts form data to Client object
         */

        try {
            System.out.println("=== PROCESSING NEW CLIENT ===");
            System.out.println("Client Name: " + client.getClientName());
            System.out.println("Client Type: " + client.getClientType());
            System.out.println("PAN: " + client.getPanNumber());
            System.out.println("Email: " + client.getEmail());

            // Basic validation
            if (client.getClientName() == null || client.getClientName().trim().isEmpty()) {
                model.addAttribute("error", "Client name is required");
                return "add-client";
            }

            if (client.getPanNumber() == null || client.getPanNumber().trim().isEmpty()) {
                model.addAttribute("error", "PAN number is required");
                return "add-client";
            }

            // Check if PAN already exists
            if (clientRepository.existsByPanNumber(client.getPanNumber())) {
                model.addAttribute("error", "Client with this PAN number already exists");
                model.addAttribute("client", client); // Keep form data
                return "add-client";
            }

            // Check if email already exists
            if (client.getEmail() != null && !client.getEmail().trim().isEmpty()) {
                if (clientRepository.existsByEmail(client.getEmail())) {
                    model.addAttribute("error", "Client with this email already exists");
                    model.addAttribute("client", client); // Keep form data
                    return "add-client";
                }
            }

            // Save the client to database
            Client savedClient = clientRepository.save(client);

            System.out.println("Client saved successfully with ID: " + savedClient.getId());
            System.out.println("=== END PROCESSING ===");

            // Redirect to clients list with success message
            // PRG pattern: Post-Redirect-Get (prevents duplicate submissions)
            return "redirect:/clients?success=true";

        } catch (Exception e) {
            System.err.println("ERROR saving client: " + e.getMessage());
            e.printStackTrace();

            // Return to form with error message
            model.addAttribute("error", "Failed to save client. Please try again.");
            model.addAttribute("client", client); // Keep form data
            return "add-client";
        }

    }

    // ADD THESE TWO NEW METHODS TO YOUR DashboardController.java
// Place them after your existing addClient() method

    @GetMapping("/clients/{id}/edit")
    public String showEditClientForm(@PathVariable("id") Long id, Model model) {
        try {
            Client client = clientRepository.findById(id).orElse(null);

            if (client == null) {
                return "redirect:/clients?error=notfound";
            }

            model.addAttribute("client", client);
            model.addAttribute("isEdit", true);  // ← Make sure this line is there!

            System.out.println("Edit mode - isEdit flag set to: true");
            System.out.println("Client ID: " + client.getId());

            return "add-client";

        } catch (Exception e) {
            System.err.println("ERROR showing edit client form: " + e.getMessage());
            return "redirect:/clients?error=true";
        }
    }

    @PostMapping("/clients/{id}/update")
    public String updateClient(@PathVariable("id") Long id,
                               @ModelAttribute("client") Client client,
                               Model model) {
        /**
         * @PathVariable EXPLANATION:
         *
         * This handles POST requests to URLs like: /clients/123/update
         * @PathVariable("id") Long id → Captures "123" from URL
         *
         * So when form submits to /clients/5/update, then id = 5L
         */

        try {
            // Set the ID from URL to the client object (important for update!)
            client.setId(id);

            System.out.println("=== UPDATING CLIENT ===");
            System.out.println("Client ID from URL: " + id);
            System.out.println("Client Name: " + client.getClientName());

            // Basic validation (same as add, but for updates)
            if (client.getClientName() == null || client.getClientName().trim().isEmpty()) {
                model.addAttribute("error", "Client name is required");
                model.addAttribute("isEdit", true);
                return "add-client";
            }

            if (client.getPanNumber() == null || client.getPanNumber().trim().isEmpty()) {
                model.addAttribute("error", "PAN number is required");
                model.addAttribute("isEdit", true);
                return "add-client";
            }

            // Check if PAN already exists (but exclude current client from check)
            Client existingPanClient = clientRepository.findByPanNumber(client.getPanNumber()).orElse(null);
            if (existingPanClient != null && !existingPanClient.getId().equals(id)) {
                model.addAttribute("error", "Another client with this PAN number already exists");
                model.addAttribute("client", client);
                model.addAttribute("isEdit", true);
                return "add-client";
            }

            // Update the client in database
            Client updatedClient = clientRepository.save(client);

            System.out.println("Client updated successfully!");
            System.out.println("=== END UPDATING ===");

            // Redirect with success message
            return "redirect:/clients?updated=true";

        } catch (Exception e) {
            System.err.println("ERROR updating client: " + e.getMessage());

            model.addAttribute("error", "Failed to update client. Please try again.");
            model.addAttribute("client", client);
            model.addAttribute("isEdit", true);
            return "add-client";
        }


    }

    @GetMapping("/clients/{id}/view")
    public String viewClient(@PathVariable("id") Long id, Model model) {
        /**
         * VIEW CLIENT DETAILS:
         *
         * WHY: User wants to see complete client information without editing
         * WHAT: Find client and display all details in read-only format
         * HOW: Pass client data to a view-only template
         */

        try {
            System.out.println("=== VIEWING CLIENT ===");
            System.out.println("Client ID: " + id);

            // Find client by ID
            Client client = clientRepository.findById(id).orElse(null);

            if (client == null) {
                System.err.println("Client not found with ID: " + id);
                return "redirect:/clients?error=notfound";
            }

            // Add client to model for template
            model.addAttribute("client", client);

            System.out.println("Found client: " + client.getClientName());
            System.out.println("Client type: " + client.getClientType());

            return "view-client"; // Will create this template next

        } catch (Exception e) {
            System.err.println("ERROR viewing client: " + e.getMessage());
            e.printStackTrace();
            return "redirect:/clients?error=true";
        }
    }

    @Autowired
    private ClientServiceRepository clientServiceRepository;

    // Show assign service form
    @GetMapping("/services/{serviceId}/assign")
    public String showAssignServiceForm(@PathVariable("serviceId") Long serviceId, Model model) {
        /**
         * SHOW SERVICE ASSIGNMENT FORM:
         *
         * User clicked "Assign to Client" button on a service
         * Show form to select client and set assignment details
         */

        try {
            // Find the service to assign
            Service service = serviceRepository.findById(serviceId).orElse(null);
            if (service == null) {
                return "redirect:/services?error=servicenotfound";
            }

            // Get all active clients for dropdown
            List<Client> activeClients = clientRepository.findByStatusOrderByClientNameAsc(Client.ClientStatus.ACTIVE);

            // Create new assignment object
            ClientService assignment = new ClientService();
            assignment.setService(service);
            assignment.setQuotedPrice(service.getBasePrice()); // Default to base price

            // Add to model
            model.addAttribute("service", service);
            model.addAttribute("clients", activeClients);
            model.addAttribute("assignment", assignment);

            return "assign-service";

        } catch (Exception e) {
            System.err.println("ERROR showing assign service form: " + e.getMessage());
            return "redirect:/services?error=true";
        }
    }

    // Process service assignment
    @PostMapping("/services/assign")
    public String assignService(@ModelAttribute("assignment") ClientService assignment,
                                @RequestParam("clientId") Long clientId,
                                @RequestParam("serviceId") Long serviceId,
                                Model model) {
        /**
         * PROCESS SERVICE ASSIGNMENT:
         *
         * Save the service assignment to database
         */

        try {
            // Find client and service
            Client client = clientRepository.findById(clientId).orElse(null);
            Service service = serviceRepository.findById(serviceId).orElse(null);

            if (client == null || service == null) {
                model.addAttribute("error", "Client or Service not found");
                return "assign-service";
            }

            // Check if this client already has this service assigned
            if (clientServiceRepository.existsByClientAndService(client, service)) {
                model.addAttribute("error", "This service is already assigned to this client");
                model.addAttribute("service", service);
                model.addAttribute("clients", clientRepository.findByStatusOrderByClientNameAsc(Client.ClientStatus.ACTIVE));
                model.addAttribute("assignment", assignment);
                return "assign-service";
            }

            // Set the client and service
            assignment.setClient(client);
            assignment.setService(service);

            // Set default due date if not provided
            if (assignment.getDueDate() == null && service.getEstimatedDurationDays() != null) {
                assignment.setDueDate(LocalDate.now().plusDays(service.getEstimatedDurationDays()));
            }

            // Save the assignment
            ClientService savedAssignment = clientServiceRepository.save(assignment);

            System.out.println("Service assigned successfully!");
            System.out.println("Client: " + client.getClientName());
            System.out.println("Service: " + service.getServiceName());
            System.out.println("Assignment ID: " + savedAssignment.getId());

            return "redirect:/clients/" + clientId + "/services?assigned=true";

        } catch (Exception e) {
            System.err.println("ERROR assigning service: " + e.getMessage());
            e.printStackTrace();

            model.addAttribute("error", "Failed to assign service. Please try again.");
            return "assign-service";
        }
    }

    // View client's services
    @GetMapping("/clients/{clientId}/services")
    public String clientServices(@PathVariable("clientId") Long clientId, Model model) {
        /**
         * VIEW CLIENT'S ASSIGNED SERVICES:
         *
         * Show all services assigned to this client
         */

        try {
            // Find the client
            Client client = clientRepository.findById(clientId).orElse(null);
            if (client == null) {
                return "redirect:/clients?error=notfound";
            }

            // Get all services assigned to this client
            List<ClientService> assignments = clientServiceRepository.findByClientOrderByAssignedDateDesc(client);

            // Get statistics
            long totalAssignments = assignments.size();
            long activeAssignments = assignments.stream()
                    .mapToLong(a -> a.getStatus() == ClientService.ServiceStatus.COMPLETED ? 0 : 1)
                    .sum();
            long completedAssignments = assignments.stream()
                    .mapToLong(a -> a.getStatus() == ClientService.ServiceStatus.COMPLETED ? 1 : 0)
                    .sum();
            long overdueAssignments = assignments.stream()
                    .mapToLong(a -> a.isOverdue() ? 1 : 0)
                    .sum();

            // Add to model
            model.addAttribute("client", client);
            model.addAttribute("assignments", assignments);
            model.addAttribute("totalAssignments", totalAssignments);
            model.addAttribute("activeAssignments", activeAssignments);
            model.addAttribute("completedAssignments", completedAssignments);
            model.addAttribute("overdueAssignments", overdueAssignments);

            return "client-services";

        } catch (Exception e) {
            System.err.println("ERROR viewing client services: " + e.getMessage());
            return "redirect:/clients?error=true";
        }
    }

    // Update service status
    @PostMapping("/assignments/{assignmentId}/status")
    public String updateServiceStatus(@PathVariable("assignmentId") Long assignmentId,
                                      @RequestParam("status") String statusStr,
                                      @RequestParam(value = "clientId", required = false) Long clientId) {
        /**
         * UPDATE SERVICE STATUS:
         *
         * Mark service as In Progress, Completed, etc.
         */

        try {
            ClientService assignment = clientServiceRepository.findById(assignmentId).orElse(null);
            if (assignment == null) {
                return "redirect:/clients?error=notfound";
            }

            // Update status
            ClientService.ServiceStatus newStatus = ClientService.ServiceStatus.valueOf(statusStr);
            assignment.setStatus(newStatus);

            // Set completion date if completed
            if (newStatus == ClientService.ServiceStatus.COMPLETED) {
                assignment.setCompletionDate(LocalDate.now());
            }

            clientServiceRepository.save(assignment);

            // Redirect back to client services page
            Long redirectClientId = clientId != null ? clientId : assignment.getClient().getId();
            return "redirect:/clients/" + redirectClientId + "/services?updated=true";

        } catch (Exception e) {
            System.err.println("ERROR updating service status: " + e.getMessage());
            return "redirect:/clients?error=true";
        }
    }
}