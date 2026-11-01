package com.shopease.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/** Delivery address entered on the checkout page. */
public class CheckoutForm {

    @NotBlank(message = "Please enter your full name")
    @Size(max = 100)
    private String fullName;

    @NotBlank(message = "Please enter your mobile number")
    @Pattern(regexp = "^[6-9][0-9]{9}$", message = "Enter a valid 10-digit mobile number")
    private String phone;

    @NotBlank(message = "Please enter your address")
    @Size(max = 500)
    private String address;

    @NotBlank(message = "Please enter your city")
    @Size(max = 100)
    private String city;

    @NotBlank(message = "Please enter your state")
    @Size(max = 100)
    private String state;

    @NotBlank(message = "Please enter your pincode")
    @Pattern(regexp = "^[1-9][0-9]{5}$", message = "Enter a valid 6-digit pincode")
    private String pincode;

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }
    public String getCity() { return city; }
    public void setCity(String city) { this.city = city; }
    public String getState() { return state; }
    public void setState(String state) { this.state = state; }
    public String getPincode() { return pincode; }
    public void setPincode(String pincode) { this.pincode = pincode; }
}
