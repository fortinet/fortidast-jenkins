package com.fortidast;
/**
* will get called when unable to establish connection.
*/
public class ConnectionException extends RuntimeException{
    /**
    * constructor.
    */
    public ConnectionException(){
        super(MessageManager.getString("cannot.connect.to.application"));
    }
    /**
    * parameterized constructor.
    * @param message Exception message to be displayed when the exception is raised
    */
    public ConnectionException(String message){
        super(message);
    }
}