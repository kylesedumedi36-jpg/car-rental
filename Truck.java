package com.mycompany.rentalmanagementapp;

public class Truck extends Vehicle implements Invoiceable {
    private double loadCapacityTons;

    public Truck(String regNo, String make, String model, double dailyRate, double loadCapacityTons) {
        super(regNo, make, model, dailyRate);
        this.loadCapacityTons = loadCapacityTons;
    }

    @Override
    public double calculateRentalCost(int days, int month) {
        double subtotal = (dailyRate + (loadCapacityTons * 45.0)) * days;
        double total = subtotal * getSeasonalMultiplier(month);

        if (days >= 7) {
            total *= 0.95;
        }
        return total;
    }

    @Override
    public String generateInvoice(int days, int month) {
        double loadCharge = loadCapacityTons * 45.0;
        double base = (dailyRate + loadCharge) * days;
        double seasonal = getSeasonalMultiplier(month);
        double beforeDiscount = base * seasonal;
        double discount = days >= 7 ? beforeDiscount * 0.05 : 0.0;
        double total = beforeDiscount - discount;

        return String.format(
                "========== RENTAL INVOICE ==========%n" +
                "Vehicle Type: Truck%n" +
                "Registration: %s%n" +
                "Make: %s%n" +
                "Model: %s%n" +
                "Rental Days: %d%n" +
                "Month: %d%n" +
                "Daily Rate: R%.2f%n" +
                "Load Capacity: %.2f tons%n" +
                "Load Charge/Day: R%.2f%n" +
                "Seasonal Multiplier: %.2f%n" +
                "Amount Before Discount: R%.2f%n" +
                "Long-Rental Discount: R%.2f%n" +
                "Total Cost: R%.2f%n" +
                "====================================%n",
                registrationNumber, make, model, days, month,
                dailyRate, loadCapacityTons, loadCharge, seasonal,
                beforeDiscount, discount, total);
    }

    public double getLoadCapacityTons() {
        return loadCapacityTons;
    }
}
