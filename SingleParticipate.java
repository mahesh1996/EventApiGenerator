import java.io.*;
public class SingleParticipate {
    private String table;
    private static final String LOGGED_SESSION = "isLoggedIn";
    private static final String ACTION = "action";
    private static final String HOST = "$host";
    private static final String DB = "$db";
    private static final String DB_USER = "$dbuser";
    private static final String DB_PASSWORD = "$dbpassword";
    private StringBuilder data = new StringBuilder();
    public SingleParticipate(String table) {
        this.table = table;
    }

    /*public static void main(String args[]){
        new SingleParticipate("user").generateApi();
    }*/

    public void generateApi() {
        this.addPreProcessData();
        this.addRegisterData();
        this.addUnregisterData();
        this.addStatusData();
        try{
            BufferedWriter bw = new BufferedWriter(new FileWriter(this.table + ".php"));
            bw.write(this.data.toString());
            bw.flush();
        }
        catch(Exception e){}
    }

    public void addPreProcessData() {
        this.data.append("<?php\n")
        .append("session_start();\n")
        .append("if(isset($_SESSION['"+ LOGGED_SESSION +"']) && !empty($_SESSION['"+ LOGGED_SESSION +"'])){\n")
        .append("    $email = $_SESSION['"+ LOGGED_SESSION +"'];\n")
        .append("    if(isset($_GET['"+ ACTION +"']) && !empty($_GET['"+ ACTION +"'])){\n")
        .append("        $status = $_GET['"+ ACTION +"'] ;\n");
    }

    public void addRegisterData() {
        this.data.append("        if($status == 'register'){").append("\n")
        .append("            try{").append("\n")
        .append("                $con = new PDO('mysql:host='."+HOST+".';dbname='."+DB+","+DB_USER+","+DB_PASSWORD+");").append("\n")
        .append("                $con->setAttribute(PDO::ATTR_ERRMODE, PDO::ERRMODE_EXCEPTION);").append("\n")
        .append("                $check_query = $con->prepare(\"select * from "+ this.table +" where email1='\".$email.\"'\");").append("\n")
        .append("                $check_query->execute();").append("\n")
        .append("                if($check_query->rowCount() == 0){").append("\n")
        .append("                    $register_query = $con->prepare(\"insert into "+this.table+"(email1) values(:email)\");").append("\n")
        .append("                    $register_query->bindParam(':email', $email);").append("\n")
        .append("                    $status = $register_query->execute();").append("\n")
        .append("                    if($status){").append("\n")
        .append("                        echo json_encode( array('status' => 'registered'));").append("\n").append("\n")
        .append("                    }").append("\n")
        .append("                    else").append("\n")
        .append("                        echo json_encode( array('status' => 'error'));").append("\n")
        .append("                }").append("\n")
        .append("                else if($check_query->rowCount() > 0) {").append("\n")
        .append("                    echo json_encode( array('status' => 'registered'));").append("\n")
        .append("                }").append("\n")
        .append("            }").append("\n")
        .append("            catch(PDOException $e){").append("\n")
        .append("                echo $e->getMessage();").append("\n")
        .append("            }").append("\n")
        .append("        }").append("\n");
    }

    public void addUnregisterData() {
        this.data.append("        else if($status == 'unregister'){").append("\n")
        .append("            try{").append("\n")
        .append("                $con = new PDO('mysql:host='."+HOST+".';dbname='."+DB+","+DB_USER+","+DB_PASSWORD+");").append("\n")
        .append("                $con->setAttribute(PDO::ATTR_ERRMODE, PDO::ERRMODE_EXCEPTION);").append("\n")
        .append("                $check_query = $con->prepare(\"select * from "+this.table+" where email1='\".$email.\"'\");").append("\n")
        .append("                $check_query->execute();").append("\n")
        .append("                if($check_query->rowCount() == 1){").append("\n")
        .append("                    $register_query = $con->prepare(\"delete from "+this.table+" where email1=:email\");").append("\n")
        .append("                    $register_query->bindParam(':email', $email);").append("\n")
        .append("                    $status = $register_query->execute();").append("\n")
        .append("                    if($status){").append("\n")
        .append("                        echo json_encode( array('status' => 'unregistered'));").append("\n")
        .append("                    }").append("\n")
        .append("                    else").append("\n")
        .append("                        echo json_encode( array('status' => 'error'));").append("\n")
        .append("                }").append("\n")
        .append("                else").append("\n")
        .append("                    echo json_encode( array('status' => 'unregistered'));").append("\n")
        .append("            }").append("\n")
        .append("            catch(PDOException $e){").append("\n")
        .append("                echo $e->getMessage();").append("\n")
        .append("            }").append("\n")
        .append("        }").append("\n");
    }

    public void addStatusData() {
        this.data.append("        else if($status == 'getstatus'){").append("\n")
        .append("            try{").append("\n")
        .append("                $con = new PDO('mysql:host='."+HOST+".';dbname='."+DB+","+DB_USER+","+DB_PASSWORD+");").append("\n").append("\n")
        .append("                $con->setAttribute(PDO::ATTR_ERRMODE, PDO::ERRMODE_EXCEPTION);").append("\n")
        .append("                $check_query = $con->prepare(\"select * from "+ this.table +" where email1='\".$email.\"'\");").append("\n")
        .append("                $check_query->execute();").append("\n")
        .append("                if($check_query->rowCount() == 1){").append("\n")
        .append("                    echo json_encode( array('status' => registered));").append("\n")
        .append("                }").append("\n")
        .append("                else if($check_query->rowCount() == 0){").append("\n")
        .append("                    echo json_encode( array('status' => unregistered));").append("\n")
        .append("                }").append("\n")
        .append("            }").append("\n")
        .append("            catch(PDOException $e){").append("\n")
        .append("                echo $e->getMessage();").append("\n")
        .append("            }").append("\n")
        .append("        }").append("\n")
        .append("    }").append("\n")
        .append("}").append("\n")
        .append("?>").append("\n");
    }

}