package utilty;

import android.app.Application;

import androidx.annotation.NonNull;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

import objects.Expense;
import objects.ExpenseModel;
import objects.User;

public class ApiLogin extends Application{
    private String username;
    private String userId;
    private Double userIncome;
    private String utaId;
    private String email;
    private String imageUrl;
    private static ApiLogin instance;

    public static ApiLogin getInstance()
    {
        if( instance == null )
            instance = new ApiLogin();
        return instance;
    }

    public ApiLogin(){} //Empty contsructor


    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getutaId() {
        return utaId;
    }

    public void setutaId(String utaId) {
        this.utaId = utaId;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public double getUserIncome() { return userIncome; }

    public void setUserIncome(Double userIncome){ this.userIncome = userIncome; }

    public String getUserEmail() {
        return email;
    }

    public void setUserEmail(String email) {
        this.email = email;
    }
}
