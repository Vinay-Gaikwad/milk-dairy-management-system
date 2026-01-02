package com.milk.milkrun.customerdatabase;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;
import androidx.room.Delete;

import java.util.List;

@Dao
public interface CustomerDao {

    @Insert
    void insert(Customer customer);

    @Update
    void update(Customer customer);

    @Delete
    void delete(Customer customer);

    // Renamed to match method names used in UpdateBillDialog
    @Query("SELECT * FROM customers WHERE number = :number LIMIT 1")
    Customer findCustomerByNumber(String number);

    @Query("SELECT * FROM customers WHERE name = :name LIMIT 1")
    Customer findCustomerByName(String name);

    @Query("SELECT * FROM customers")
    List<Customer> getAllCustomers();

    @Query("SELECT * FROM customers WHERE number = :number LIMIT 1")
    Customer getCustomerByNumber(String number);

    @Query("SELECT * FROM customers WHERE name = :name LIMIT 1")
    Customer getCustomerByName(String name);  // ← this is the required method

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertOrUpdate(Customer customer);

    @Query("DELETE FROM customers")
    void deleteAll();

    @Update
    void updateCustomer(Customer customer);




}
