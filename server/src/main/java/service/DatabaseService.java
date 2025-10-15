package service;

import dataaccess.DataAccess;
import datamodel.RegisterResponse;
import model.AuthData;
import model.UserData;

import java.util.UUID;

public class DatabaseService {
    private final DataAccess dataAccess;
    public DatabaseService(DataAccess dataAccess){
        this.dataAccess = dataAccess;
    }

    public void clear(){
        dataAccess.clear();
    }
}
