package com.bcit.orderly;

import com.google.firebase.firestore.DocumentReference;

import org.w3c.dom.Document;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class User {

    public String name;
    public String email;
    public Boolean isOnline;
    public List<DocumentReference> projects;
    public String id;

    public User() {
        // Default constructor required for calls to DataSnapshot.getValue(User.class)
    }

    /**
     * Public constructor to create a User instance.
     *
     * @param name Name of the user.
     * @param email Email of the user.
     * @param isOnline A flag representing the user's online status.
     * @param projects Projects associated to the user.
     */
    public User(String name, String email, Boolean isOnline, List<DocumentReference> projects) {
        this.name = name;
        this.email = email;
        this.isOnline = isOnline;
        this.projects = projects;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    public Boolean getIsOnline() {
        return isOnline;
    }

    public List<DocumentReference> getProjects() {
        return projects;
    }

    public void setProjects(List<DocumentReference> new_projects) {
        projects = new_projects;
    }

    public void setId(String docId) {
        this.id = docId;
    }

    public String getId() { return id; }

    public List<String> getProjectIds() {
        DocumentReference[] ref_array = projects.toArray(new DocumentReference[] {} );
        return (List<String>) Arrays.stream(ref_array)
                                    .map(DocumentReference::getId).collect(Collectors.toList());
    }

    @Override
    public String toString() {
        return "User{" +
                "name='" + name + '\'' +
                ", email='" + email + '\'' +
                ", isOnline=" + isOnline +
                ", projects=" + projects +
                '}';
    }
}
