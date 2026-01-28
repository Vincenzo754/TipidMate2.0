package com.example.tipidmate.api;

import com.example.tipidmate.models.ApiResponse;
import com.example.tipidmate.models.Category;
import com.example.tipidmate.models.Contribution;
import com.example.tipidmate.models.Expense;
import com.example.tipidmate.models.Goal;
import com.example.tipidmate.models.GroupBudget;
import com.example.tipidmate.models.GroupBudgetContribution;
import com.example.tipidmate.models.GroupBudgetMember;
import com.example.tipidmate.models.Transaction;  // ← ADD THIS IMPORT
import com.example.tipidmate.models.User;
import com.google.gson.annotations.SerializedName;  // ← ADD THIS IMPORT

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.HTTP;  // ← ADD THIS IMPORT
import retrofit2.http.POST;
import retrofit2.http.Query;

public interface ApiService {

    // ========== USER ENDPOINTS ==========
    @POST("register.php")
    Call<ApiResponse<User>> registerUser(@Body User user);

    @POST("login.php")
    Call<ApiResponse<User>> loginUser(@Body User user);

    // ========== EXPENSE ENDPOINTS ==========
    @POST("add_expense.php")
    Call<ApiResponse<Expense>> addExpense(@Body Expense expense);

    @GET("get_expenses.php")
    Call<ApiResponse<List<Expense>>> getExpenses(@Query("user_id") int userId);

    @GET("get_categories.php")
    Call<ApiResponse<List<Category>>> getCategories();

    // ========== GOAL ENDPOINTS ==========
    @GET("get_goals.php")
    Call<ApiResponse<List<Goal>>> getGoals(@Query("user_id") int userId);

    @POST("add_goal.php")
    Call<ApiResponse<Goal>> addGoal(@Body Goal goal);

    @POST("update_goal.php")
    Call<ApiResponse<Goal>> updateGoal(@Body Goal goal);

    @POST("delete_goal.php")
    Call<ApiResponse<Void>> deleteGoal(@Body Goal goal);

    // ========== GOAL CONTRIBUTION ENDPOINTS ==========
    @POST("add_contribution.php")
    Call<ApiResponse<Contribution>> addContribution(@Body Contribution contribution);

    @GET("get_contributions.php")
    Call<ApiResponse<List<Contribution>>> getContributions(@Query("goal_id") int goalId);

    // ========== GROUP BUDGET ENDPOINTS ==========
    @GET("get_group_budgets.php")
    Call<ApiResponse<List<GroupBudget>>> getGroupBudgets(@Query("user_id") int userId);

    @POST("add_group_budget.php")
    Call<ApiResponse<GroupBudget>> addGroupBudget(@Body GroupBudget groupBudget);

    @POST("delete_group_budget.php")
    Call<ApiResponse<Void>> deleteGroupBudget(@Body GroupBudget groupBudget);

    // ========== GROUP BUDGET MEMBER ENDPOINTS ==========
    @POST("add_member.php")
    Call<ApiResponse<GroupBudgetMember>> addMember(@Body GroupBudgetMember member);

    @GET("get_members.php")
    Call<ApiResponse<List<GroupBudgetMember>>> getMembers(@Query("group_budget_id") int groupBudgetId);

    // ========== GROUP BUDGET CONTRIBUTION ENDPOINTS ==========
    @POST("add_group_contribution.php")
    Call<ApiResponse<GroupBudgetContribution>> addGroupContribution(@Body GroupBudgetContribution contribution);

    @GET("get_group_contributions.php")
    Call<ApiResponse<List<GroupBudgetContribution>>> getGroupContributions(@Query("group_budget_id") int groupBudgetId);

    // ========== TRANSACTION ENDPOINTS ==========
    // ← ADD THESE NEW ENDPOINTS HERE

    @POST("add_transaction.php")
    Call<ApiResponse<Transaction>> addTransaction(@Body Transaction transaction);

    @GET("get_transactions.php")
    Call<ApiResponse<List<Transaction>>> getTransactions(@Query("user_id") int userId);

    @HTTP(method = "DELETE", path = "delete_transaction.php", hasBody = true)
    Call<ApiResponse<Void>> deleteTransaction(@Body DeleteRequest deleteRequest);

    // ========== HELPER CLASSES ==========
    // ← ADD THIS HELPER CLASS HERE

    class DeleteRequest {
        @SerializedName("transaction_id")
        private int transactionId;

        public DeleteRequest(int transactionId) {
            this.transactionId = transactionId;
        }

        public int getTransactionId() {
            return transactionId;
        }
    }
}