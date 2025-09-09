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
            // Get real data from database
            long totalClients = clientRepository.countByStatus(Client.ClientStatus.ACTIVE);
            long totalServices = serviceRepository.countByStatus(Service.ServiceStatus.ACTIVE);

            // For demo purposes, we'll simulate pending tasks and deadlines
            int pendingTasks = 8; // TODO: Replace with real task count
            int upcomingDeadlines = 3; // TODO: Replace with real deadline count

            // Add data to model
            model.addAttribute("welcomeMessage", "Welcome to CA Practice Management System");
            model.addAttribute("totalClients", totalClients);
            model.addAttribute("totalServices", totalServices);
            model.addAttribute("pendingTasks", pendingTasks);
            model.addAttribute("upcomingDeadlines", upcomingDeadlines);

            return "dashboard";

        } catch (Exception e) {
            System.err.println("ERROR in dashboard method: " + e.getMessage());
            e.printStackTrace();

            // Fallback with static data
            model.addAttribute("welcomeMessage", "Welcome (Fallback Mode)");
            model.addAttribute("totalClients", 0);
            model.addAttribute("totalServices", 0);
            model.addAttribute("pendingTasks", 0);
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
    public String clients(Model model) {
        try {
            // Get all clients ordered by name
            List<Client> allClients = clientRepository.findAllByOrderByClientNameAsc();

            // Get statistics
            long totalClients = clientRepository.count();
            long activeClients = clientRepository.countByStatus(Client.ClientStatus.ACTIVE);
            long inactiveClients = clientRepository.countByStatus(Client.ClientStatus.INACTIVE);

            // Add to model
            model.addAttribute("clients", allClients);
            model.addAttribute("totalClients", totalClients);
            model.addAttribute("activeClients", activeClients);
            model.addAttribute("inactiveClients", inactiveClients);

            return "clients";

        } catch (Exception e) {
            System.err.println("ERROR in clients method: " + e.getMessage());
            model.addAttribute("clients", List.of());
            model.addAttribute("totalClients", 0);
            model.addAttribute("activeClients", 0);
            model.addAttribute("inactiveClients", 0);
            return "clients";
        }
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
}