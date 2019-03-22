/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Classes;

import java.io.Serializable;
import java.util.Date;

/**
 *
 * @author Corey
 */
public class Note implements Serializable {

    private String note;
    private Date date;

    public Note(String note, Date date) {
        this.setNote(note);
        this.setDate(date);
    }

    public Note() {
        this.setDate(null);
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }
}
