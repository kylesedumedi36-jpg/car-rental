package com.mycompany.rentalmanagementapp;

import java.io.*;
import java.util.ArrayList;
import java.util.Scanner;

/**
 * Rosebank Wheels - Vehicle Rental Management System
 */
public class RentalManagementApp {

    private static final ArrayList<Vehicle> fleet = new ArrayList<>();
    private static double[][] monthlyRevenue = new double[0][12];
    private static final ArrayList<String> invoices = new ArrayList<>();
    private static final Scanner scanner = new Scanner(System.in);

    public static void main(String[] args) {
        boolean running = true;

        System.out.println("==============================================");
        System.out.println("       ROSEBANK WHEELS RENTAL SYSTEM");
        System.out.println("==============================================");

        while (running) {
            displayMenu();

            try {
                int choice = Integer.parseInt(scanner.nextLine().trim());

                switch (choice) {
                    case 1:
                        System.out.print("Enter fleet filename (default fleet.txt): ");
                        String filename = scanner.nextLine().trim();
                        if (filename.isEmpty()) {
                            filename = "fleet.txt";
                        }
                        loadFleetFromFile(filename);
                        break;

                    case 2:
                        viewAllVehicles();
                        break;

                    case 3:
                        bookVehicle();
                        break;

                    case 4:
                        viewRevenueReport();
                        break;

                    case 5:
                        System.out.print("Enter output filename (default invoices.txt): ");
                        String output = scanner.nextLine().trim();
                        if (output.isEmpty()) {
                            output = "invoices.txt";
                        }
                        saveInvoicesToFile(output);
                        break;

                    case 6:
                        running = false;
                        System.out.println("Thank you for using Rosebank Wheels. Goodbye!");
                        break;

                    default:
                        System.out.println("Invalid menu choice. Please enter a number from 1 to 6.");
                }
            } catch (NumberFormatException e) {
                System.out.println("Invalid input. Please enter a menu number from 1 to 6.");
            } catch (Exception e) {
                System.out.println("Unexpected input problem: " + e.getMessage());
            }
        }

        scanner.close();
    }

    private static void displayMenu() {
        System.out.println();
        System.out.println("===== ROSEBANK WHEELS MENU =====");
        System.out.println("1. Load fleet from file");
        System.out.println("2. View all vehicles");
        System.out.println("3. Book a vehicle");
        System.out.println("4. View revenue report");
        System.out.println("5. Save all invoices to file");
        System.out.println("6. Exit");
        System.out.print("Enter your choice: ");
    }

    public static void loadFleetFromFile(String filename) {
        ArrayList<Vehicle> loadedVehicles = new ArrayList<>();

        try (BufferedReader reader = new BufferedReader(new FileReader(filename))) {
            String line;
            int lineNumber = 0;

            while ((line = reader.readLine()) != null) {
                lineNumber++;
                line = line.trim();

                if (line.isEmpty() || line.startsWith("#")) {
                    continue;
                }

                String[] parts = line.split(",", -1);

                if (parts.length != 6) {
                    System.out.println("Warning: Line " + lineNumber
                            + " skipped - expected 6 comma-separated values.");
                    continue;
                }

                try {
                    String type = parts[0].trim();
                    String regNo = parts[1].trim();
                    String make = parts[2].trim();
                    String model = parts[3].trim();
                    double dailyRate = Double.parseDouble(parts[4].trim());
                    double extraValue = Double.parseDouble(parts[5].trim());

                    if (type.equalsIgnoreCase("Car")) {
                        loadedVehicles.add(new Car(regNo, make, model, dailyRate, extraValue));
                    } else if (type.equalsIgnoreCase("Truck")) {
                        loadedVehicles.add(new Truck(regNo, make, model, dailyRate, extraValue));
                    } else {
                        throw new InvalidBookingException(
                                "Unknown vehicle type '" + type + "' on line " + lineNumber);
                    }

                } catch (NumberFormatException e) {
                    System.out.println("Warning: Line " + lineNumber
                            + " skipped - daily rate or extra value is not a valid number.");
                } catch (InvalidBookingException e) {
                    System.out.println("Warning: " + e.getMessage() + ". Line skipped.");
                }
            }

            fleet.clear();
            fleet.addAll(loadedVehicles);
            monthlyRevenue = new double[fleet.size()][12];

            System.out.println("Fleet loading completed.");
            System.out.println("Valid vehicles loaded: " + fleet.size());
            System.out.println("Invalid lines were skipped without crashing the application.");

        } catch (FileNotFoundException e) {
            System.out.println("Fleet file not found: " + filename);
            System.out.println("Please check the filename and make sure the file is in the project folder.");
        } catch (IOException e) {
            System.out.println("Could not read the fleet file: " + e.getMessage());
        }
    }

    private static void viewAllVehicles() {
        if (fleet.isEmpty()) {
            System.out.println("No vehicles are currently loaded.");
            return;
        }

        System.out.println();
        System.out.println("========== VEHICLE FLEET ==========");
        for (Vehicle vehicle : fleet) {
            vehicle.displaySummary();

            if (vehicle instanceof Car) {
                Car car = (Car) vehicle;
                System.out.printf("   Type: Car | Insurance/Day: R%.2f%n",
                        car.getInsuranceFeePerDay());
            } else if (vehicle instanceof Truck) {
                Truck truck = (Truck) vehicle;
                System.out.printf("   Type: Truck | Capacity: %.2f tons%n",
                        truck.getLoadCapacityTons());
            }
        }
        System.out.println("===================================");
    }

    private static void bookVehicle() {
        if (fleet.isEmpty()) {
            System.out.println("No vehicles are loaded. Load the fleet first.");
            return;
        }

        try {
            System.out.print("Enter vehicle registration number: ");
            String regNo = scanner.nextLine().trim();

            Vehicle selected = findVehicle(regNo);

            if (selected == null) {
                throw new InvalidBookingException(
                        "No vehicle with registration '" + regNo + "' was found.");
            }

            System.out.print("Enter number of rental days: ");
            int days = Integer.parseInt(scanner.nextLine().trim());

            if (days <= 0) {
                throw new InvalidBookingException(
                        "Rental days must be greater than zero. Booking cancelled.");
            }

            System.out.print("Enter rental month (1-12): ");
            int month = Integer.parseInt(scanner.nextLine().trim());

            if (month < 1 || month > 12) {
                throw new InvalidBookingException(
                        "Month must be between 1 and 12. Booking cancelled.");
            }

            double cost = selected.calculateRentalCost(days, month);

            Invoiceable invoiceable = (Invoiceable) selected;
            String invoice = invoiceable.generateInvoice(days, month);

            int vehicleIndex = fleet.indexOf(selected);
            monthlyRevenue[vehicleIndex][month - 1] += cost;
            invoices.add(invoice);

            System.out.printf("%nBooking completed successfully.%n");
            System.out.printf("Vehicle: %s %s (%s)%n",
                    selected.make, selected.model, selected.registrationNumber);
            System.out.printf("Rental cost: R%.2f%n", cost);
            System.out.println();
            System.out.println(invoice);

        } catch (NumberFormatException e) {
            System.out.println("Invalid number entered. Booking was not processed.");
        } catch (InvalidBookingException e) {
            System.out.println("Booking error: " + e.getMessage());
        }
    }

    private static Vehicle findVehicle(String registrationNumber) {
        for (Vehicle vehicle : fleet) {
            if (vehicle.getRegistrationNumber().equalsIgnoreCase(registrationNumber)) {
                return vehicle;
            }
        }
        return null;
    }

    private static void viewRevenueReport() {
        if (fleet.isEmpty()) {
            System.out.println("No fleet data available. Load vehicles first.");
            return;
        }

        System.out.println();
        System.out.println("=============== REVENUE REPORT ===============");

        double companyTotal = 0.0;
        double highestRevenue = -1.0;
        int topVehicleIndex = -1;

        String[] monthNames = {
            "January", "February", "March", "April", "May", "June",
            "July", "August", "September", "October", "November", "December"
        };

        double[] monthlyTotals = new double[12];

        // Nested loops: row totals and column totals.
        for (int row = 0; row < monthlyRevenue.length; row++) {
            double vehicleTotal = 0.0;

            for (int col = 0; col < monthlyRevenue[row].length; col++) {
                vehicleTotal += monthlyRevenue[row][col];
                monthlyTotals[col] += monthlyRevenue[row][col];
            }

            companyTotal += vehicleTotal;

            if (vehicleTotal > highestRevenue) {
                highestRevenue = vehicleTotal;
                topVehicleIndex = row;
            }

            System.out.printf("%s %s (%s): R%.2f%n",
                    fleet.get(row).getMake(),
                    fleet.get(row).getModel(),
                    fleet.get(row).getRegistrationNumber(),
                    vehicleTotal);
        }

        System.out.println("-----------------------------------------------");
        System.out.println("Company revenue by month:");

        for (int col = 0; col < monthlyTotals.length; col++) {
            System.out.printf("%-10s: R%.2f%n", monthNames[col], monthlyTotals[col]);
        }

        System.out.println("-----------------------------------------------");
        System.out.printf("Company Total Revenue: R%.2f%n", companyTotal);

        if (topVehicleIndex >= 0) {
            Vehicle topVehicle = fleet.get(topVehicleIndex);
            System.out.printf("Highest-Earning Vehicle: %s %s (%s) - R%.2f%n",
                    topVehicle.getMake(),
                    topVehicle.getModel(),
                    topVehicle.getRegistrationNumber(),
                    highestRevenue);
        }

        System.out.println("================================================");
    }

    public static void saveInvoicesToFile(String filename) {
        if (invoices.isEmpty()) {
            System.out.println("There are no invoices to save yet.");
            return;
        }

        try (PrintWriter writer = new PrintWriter(new FileWriter(filename))) {
            writer.println("ROSEBANK WHEELS - SAVED RENTAL INVOICES");
            writer.println("========================================");
            writer.println();

            for (String invoice : invoices) {
                writer.println(invoice);
            }

            System.out.println(invoices.size() + " invoice(s) saved successfully to: " + filename);

        } catch (IOException e) {
            System.out.println("Could not save invoices: " + e.getMessage());
        }
    }
}
