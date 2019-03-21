/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Classes;

import java.io.Serializable;
import java.math.BigInteger;

/**
 *
 * @author Corey
 */
public class Figures extends Person implements Serializable{
    private int rating;
    private String relationship;
    private String note;
    
    public Figures(String firstname, String lastname, BigInteger phonenumber, String email, String relationship, String note, int rating){
        this.setEmail(email);
        this.setFirstname(firstname);
        this.setLastname(lastname);
        this.setPhonenumber(phonenumber);
        this.rating = rating;
        this.relationship=relationship;
        this.note=note;
    }

     public String getNote(){
         return note;
     }
     
     public void setNote(String str){
         this.note = str;
     }
     
    public void setRating(int rating) {
        this.rating = rating;
    }
    public int getRating() {
        return rating;
    }

    public String getRelationship() {
        return relationship;
    }

    public void setRelationship(String relationship) {
        this.relationship = relationship;
    }
}
