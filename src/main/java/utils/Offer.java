package utils;

import java.util.List;

public class Offer {
    private String offerId;
    private String offerLink;
    private String offerImage;
    private String offerName;
    private String category;
    private List<String> subCategories; // multiple subcategories

    // Constructor
    public Offer(String offerId, String offerLink, String offerImage, String offerName, String category, List<String> subCategories) {
        this.offerId = offerId;
        this.offerLink = offerLink;
        this.offerImage = offerImage;
        this.offerName = offerName;
        this.category = category;
        this.subCategories = subCategories;
    }

    // Default constructor
    public Offer() {
    }

    // Getters
    public String getOfferId() {
        return offerId;
    }

    public String getOfferLink() {
        return offerLink;
    }

    public String getOfferImage() {
        return offerImage;
    }

    public String getOfferName() {
        return offerName;
    }

    public String getCategory() {
        return category;
    }

    public List<String> getSubCategories() {
        return subCategories;
    }

    // Setters
    public void setOfferId(String offerId) {
        this.offerId = offerId;
    }

    public void setOfferLink(String offerLink) {
        this.offerLink = offerLink;
    }

    public void setOfferImage(String offerImage) {
        this.offerImage = offerImage;
    }

    public void setOfferName(String offerName) {
        this.offerName = offerName;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public void setSubCategories(List<String> subCategories) {
        this.subCategories = subCategories;
    }

    // toString (useful for debugging)
    @Override
    public String toString() {
        return "Offer {" +
                "offerId='" + offerId + '\'' +
                ", offerLink='" + offerLink + '\'' +
                ", offerImage='" + offerImage + '\'' +
                ", offerName='" + offerName + '\'' +
                ", category='" + category + '\'' +
                ", subCategories=" + subCategories +
                '}';
    }
}
