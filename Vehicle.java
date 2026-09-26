package com.mycompany.rentalmanagementapp;

/**
 * Abstract base class for all rentable vehicles.
 */
public abstract class Vehicle {
    protected String registrationNumber;
    protected String make;
    protected String model;
    protected double dailyRate;

    public Vehicle(String regNo, String make, String model, double dailyRate) {
        this.registrationNumber = regNo;
        this.make = make;
        this.model = model;
        this.dailyRate = dailyRate;
    }

    public double getSeasonalMultiplier(int month) {
        switch (month) {
            case 12:
            case 1:
            case 2:
                return 1.30;
            case 6:
            case 7:
                return 1.15;
            default:
                return 1.00;
        }
    }

    public abstract double calculateRentalCost(int days, int month);

    public void displaySummary() {
        System.out.printf("Registration: %s | Make: %s | Model: %s | Daily Rate: R%.2f%n",
                registrationNumber, make, model, dailyRate);
    }

    public String getRegistrationNumber() {
        return registrationNumber;
    }

    public String getMake() {
        return make;
    }

    public String getModel() {
        return model;
    }

    public double getDailyRate() {
        return dailyRate;
    }
}
