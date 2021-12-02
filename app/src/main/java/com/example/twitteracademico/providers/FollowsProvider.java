package com.example.twitteracademico.providers;

import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.example.twitteracademico.models.Follow;
import com.google.firebase.firestore.Query;

public class FollowsProvider {
    CollectionReference mCollection;

    public FollowsProvider() {
        mCollection = FirebaseFirestore.getInstance().collection("Follows");
    }

    public Task<Void> create(Follow follow){
        DocumentReference document = mCollection.document();
        String id = document.getId();
        follow.setId(id);
        return document.set(follow);
    }

    public Query getFollowsByUser (String idSiguiendo){
        return mCollection.whereEqualTo("idSiguiendo", idSiguiendo);
    }

    public Query getFollowByUsers(String idSiguiendo, String idSeguidor){
        return mCollection.whereEqualTo("idSiguiendo",idSiguiendo).whereEqualTo("idSeguidor",idSeguidor);
    }

    public Task<Void> delete(String id){
        return mCollection.document(id).delete();
    }
}
