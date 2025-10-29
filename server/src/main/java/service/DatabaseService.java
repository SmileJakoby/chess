package service;

import dataaccess.DataAccess;
import dataaccess.DataAccessException;
import datamodel.RegisterResponse;
import model.AuthData;
import model.UserData;

import java.util.UUID;

public class DatabaseService {
    private final DataAccess dataAccess;
    public DatabaseService(DataAccess dataAccess){
        this.dataAccess = dataAccess;
    }

    public void clear() throws DataAccessException {
        dataAccess.clear();
    }
}
