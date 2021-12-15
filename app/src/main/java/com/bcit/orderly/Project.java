package com.bcit.orderly;

import com.google.firebase.firestore.DocumentId;
import com.google.firebase.firestore.DocumentReference;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class Project {

    public long dueDate;
    public String name;
    public String groupCode;
    public List<DocumentReference> tasks;
    public List<DocumentReference> users;
    public String id;

    public Project() {
        // Default constructor required for calls to DataSnapshot.getValue(Project.class)
    }

    /**
     * Public constructor to create a Project instance.
     *
     * @param dueDate Due date of the project.
     * @param groupCode Alphanumeric string for the project.
     * @param name Name of the project.
     * @param tasks Tasks associated to the project.
     * @param users Users associated to the project.
     */
    public Project(String name, long dueDate, String groupCode, List<DocumentReference> tasks, List<DocumentReference> users) {
        this.name = name;
        this.dueDate = dueDate;
        this.groupCode = groupCode;
        this.tasks = tasks;
        this.users = users;
    }

    public List<DocumentReference> getTaskList() {
        return tasks;
    }

    public List<DocumentReference> getUserList() {
        return users;
    }

    public String getName() {
        return name;
    }

    public long getDueDate() {
        return dueDate;
    }

    public String getGroupCode() { return groupCode; }

    public void setId(String docId) {
        this.id = docId;
    }

    public String getId() { return id; }

    public void setUsers(List<DocumentReference> users) {
        this.users = users;
    }

    public List<String> getTaskIds() {
        DocumentReference[] ref_array = tasks.toArray(new DocumentReference[] {} );
        return (List<String>) Arrays.stream(ref_array)
                .map(DocumentReference::getId).collect(Collectors.toList());
    }

    public List<String> getUserIds() {
        DocumentReference[] ref_array = users.toArray(new DocumentReference[] {} );
        return (List<String>) Arrays.stream(ref_array)
                .map(DocumentReference::getId).collect(Collectors.toList());
    }

    @Override
    public String toString() {
        return "Project{" +
                "dueDate=" + dueDate +
                ", name='" + name + '\'' +
                ", groupCode='" + groupCode + '\'' +
                ", tasks=" + tasks +
                ", users=" + users +
                ", id='" + id + '\'' +
                '}';
    }
}
