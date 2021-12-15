package com.bcit.orderly;

import android.icu.text.SimpleDateFormat;

import com.google.firebase.firestore.DocumentReference;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

public class Task implements Serializable
{
    public String name;
    public String description;
    public boolean isCompleted;
    public DocumentReference user;
    public List<String> commentList;
    public long dueDate;
    public String id;
    public String projectId;

    public Task() {

    }

    /**
     * Public constructor to create a Task instance.
     *
     * @param name Name of the task.
     * @param description A description of the task.
     * @param isCompleted A flag representing the task's completion status.
     * @param user Users associated to the task.
     * @param commentList A list of comments associated to the task
     * @param dueDate Due date of the task.
     * @param projectId ID of the associated project.
     */
    public Task(String name, String description, boolean isCompleted, DocumentReference user, List<String> commentList, long dueDate, String projectId) {
         this.name = name;
         this.description = description;
         this.isCompleted = isCompleted;
         this.user = user;
         this.commentList = commentList;
         this.dueDate = dueDate;
         this.projectId = projectId;
    }

    public String getName() {
        return name;
    }

    public String getProjectId() { return this.projectId; }

    public void setProjectId(String projectId) {this.projectId = projectId; }

    public String getId() { return this.id; }

    public void setId(String id) {this.id = id; }

    public String getDescription() {
        return description;
    }

    public DocumentReference getUser() {
        return user;
    }

    public List<String> getCommentList() {
        return commentList;
    }

    public boolean getIsCompleted() {
        return isCompleted;
    }

    public void setTaskName(String name) {
        this.name = name;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setCompleted(boolean completed) {
        isCompleted = completed;
    }

    public void setUser(DocumentReference user) {
        this.user = user;
    }

    public void setCommentList(List<String> commentList) {
        this.commentList = commentList;
    }

    public String getDueDateAsString() {
        return new SimpleDateFormat("yyyy-MM-dd").format(new Date(this.dueDate));
    }
}
