package utils;

import java.util.ArrayList;;


public class Offer {
    private String offerId;
    private String offerLink;
    private String offerImage;
    private String offerName;
    private ArrayList<Integer> categories;     //  list of Category
    private String des;

    private int store_id;
    private String store_name;
    private String storeUrl;

    // Constructor
    public Offer(String offerId, String offerLink, String offerImage, String offerName, 
    ArrayList<Integer> categories, String des, int store_id, String store_name, String storeUrl) {
        this.offerId = offerId;
        this.offerLink = offerLink;
        this.offerImage = offerImage;
        this.offerName = offerName;
        this.categories = categories;
        this.des = des;
        this.store_id= store_id;
        this.store_name= store_name;
        this.storeUrl= storeUrl;

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

    public ArrayList<Integer> getCategory() {
        return categories;
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


    // toString (useful for debugging)
    @Override
    public String toString() {
        return  "offerName=" + offerName + 
                "\nofferId=" + offerId + 
                "\nofferLink=" + offerLink + 
                "\nofferImage=" + offerImage + 
                "\nmaincategory=" + categories +
                "\ndes=" + des +
                "\nstore_id=" + store_id +
                "\nstore_name=" + store_name + 
                "\nstoreUrl=" + storeUrl ;
                
    }
}
