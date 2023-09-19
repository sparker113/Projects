Opens a login window with password protected view with encryption and machineID authentication capabilities and flexibility in how the developer wants to authorize the input credentials by taking a Function<HashMap<String,String>,Boolean>
as a parameter in which the HashMap<String,String> is the map returned by the username and password input using static String values UserNamePassword.USERNAME and UserNamePassword.PASSWORD and then applying the input function to determine 
whether the user is authorized/authenticated or not
