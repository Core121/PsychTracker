/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Classes;

import java.io.Serializable;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Date;
/**
 *
 * @author Corey
 */
public class Client extends Person implements Serializable {

    private ArrayList<Note> notes = new ArrayList<>();
    private ArrayList<Figures> figures = new ArrayList<>();

    public Client(String firstname, String lastname, BigInteger phonenumber, String email) {
        this.setEmail(email);
        this.setFirstname(firstname);
        this.setLastname(lastname);
        this.setPhonenumber(phonenumber);
    }

    public Client() {
        this.setEmail(null);
        this.setFirstname(null);
        this.setLastname(null);
        this.setPhonenumber(new BigInteger("0"));
    }

    public ArrayList<Figures> getFigures() {
        return this.figures;
    }

    public ArrayList<Note> getNotes() {
        return this.notes;
    }

    public void AddNote(String note, Date date) {
        notes.add(new Note(note, date));
    }
}
