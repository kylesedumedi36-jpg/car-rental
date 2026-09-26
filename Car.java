package com.mycompany.rentalmanagementapp;

public class Car extends Vehicle implements Invoiceable {
    private double insuranceFeePerDay;

    public Car(String regNo, String make, String model, double dailyRate, double insuranceFeePerDay) {
        super(regNo, make, model, dailyRate);
        this.insuranceFeePerDay = insuranceFeePerDay;
    }

    @Override
    public double calculateRentalCost(int days, int month) {
        double subtotal = (dailyRate + insuranceFeePerDay) * days;
        double total = subtotal * getSeasonalMultiplier(month);

        if (days >= 7) {
            total *= 0.90;
        }
        return total;
    }

    @Override
    public String generateInvoice(int days, int month) {
        double base = (dailyRate + insuranceFeePerDay) * days;
        double seasonal = getSeasonalMultiplier(month);
        double beforeDiscount = base * seasonal;
        double discount = days >= 7 ? beforeDiscount * 0.10 : 0.0;
        double total = beforeDiscount - discount;

        return String.format(
                "========== RENTAL INVOICE ==========%n" +
                "Vehicle Type: Car%n" +
                "Registration: %s%n" +
                "Make: %s%n" +
                "Model: %s%n" +
                "Rental Days: %d%n" +
                "Month: %d%n" +
                "Daily Rate: R%.2f%n" +
                "Insurance Fee/Day: R%.2f%n" +
                "Seasonal Multiplier: %.2f%n" +
                "Amount Before Discount: R%.2f%n" +
                "Long-Rental Discount: R%.2f%n" +
                "Total Cost: R%.2f%n" +
                "====================================%n",
                registrationNumber, make, model, days, month,
                dailyRate, insuranceFeePerDay, seasonal,
                beforeDiscount, discount, total);
    }

    public double getInsuranceFeePerDay() {
        return insuranceFeePerDay;
    }
}
