package com.fortidast;
/**.
*Class for creating array of Name Value pair objects.
*/
public class NameValuePair{
    /**.
    * variable for storing the key
    */
    private String name;
    /**.
    * variable for storing the value
    */
    private String value;
    /**.
    * constructor
    * @param name  name will gets initialized when NameValuePair object created
    * @param value value will gets initialized when NameValuePair object created
    */
    public NameValuePair(String name,String value){
        this.name = name;
        this.value = value;
    }
    /**.
    * @return returns name
    */
    public String getName(){
        return name;
    }
    /**.
    * @return return value
    */
    public String getValue(){
        return value;
    }
    }
