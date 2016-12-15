import java.io.*;
import java.util.*;
public class MultiParticipate {

	private String table;
	private String userTable;
	private int noOfParticipant;
    private static final String LOGGED_SESSION = "isLoggedIn";
    private static final String ACTION = "action";
    private static final String HOST = "$host";
    private static final String DB = "$db";
    private static final String DB_USER = "$dbuser";
    private static final String DB_PASSWORD = "$dbpassword";
    private StringBuilder data = new StringBuilder();
    private ArrayList<String> partnerEmails = new ArrayList<String>();
    private ArrayList<String> partnerStatus = new ArrayList<String>();

    public MultiParticipate(String table,int noOfParticipant,String userTable) {
    	this.table = table;
    	this.userTable = userTable;
    	this.noOfParticipant = noOfParticipant;
    	for(int i = 0; i < this.noOfParticipant - 1; i++) {
    		partnerEmails.add("partner_email" + (i+1));
    		partnerStatus.add("partner" + (i+1) + "_status");
    	}
    }

	/*public static void main(String args[]) {
		MultiParticipate tmp = new MultiParticipate("quiz",3,"users");
		tmp.generateApi();
		System.out.println(tmp.data);
	}*/

	public void generateApi() {
		this.addPreProcessData();
		this.addRegisterdata();
		this.addStatusData();
		this.addUnRegisterdata();
		this.addCheckUserExist();
		this.addUserAlreadyRegisteredData();
		this.addRegisterUserData();
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

	//Register data
	public void addRegisterdata() {
		this.data.append("        if($status == 'register'){").append("\n");
		String prefix = "";
		this.data.append("            if(");

		for(String email : partnerEmails) {
			this.data.append(prefix);
			prefix = "&&";
			this.data.append(this.getIfConditionData(email));
		}

		this.data.append(") {").append("\n");
		for(String email : partnerEmails) {
			this.data.append(this.getPartnerEmails(email)).append("\n");
		}

		boolean isFirst = true;
		for(int i=0; i< partnerEmails.size(); i++) {
			this.data.append(this.getYourSelfOrNotData(partnerEmails.get(i),partnerStatus.get(i),isFirst)).append("\n");
			if(isFirst) isFirst = false;
		}

		this.data.append("                try{").append("\n")
        .append("                    $con = new PDO('mysql:host='."+HOST+".';dbname='."+DB+","+DB_USER+","+DB_PASSWORD+");").append("\n")
        .append("                    $con->setAttribute(PDO::ATTR_ERRMODE, PDO::ERRMODE_EXCEPTION);").append("\n")
        .append("                    $currentUser = null;").append("\n")
        .append("                    $get_user = $con->prepare("+ this.getUserQuery() +");").append("\n")
        .append("                    $get_user->bindParam(':email',$email);").append("\n")
        .append("                    $get_user->execute();").append("\n")
        .append("                    if($get_user->rowCount() == 0){").append("\n")
        .append("                        $register = $con->prepare("+this.getRegisterQuery()+");").append("\n")
        .append("                        $register->execute();").append("\n")
        .append("                    }").append("\n")
        .append("                    $get_user->execute();").append("\n")
        .append("                    $get_user_result = $get_user->fetchAll(PDO::FETCH_ASSOC);").append("\n")
        .append("                    foreach($get_user_result as $tempuser){").append("\n")
        .append("                        $currentUser = $tempuser;").append("\n")
        .append("                    }").append("\n")
        .append("                    if(checkUserExist($con");

        for(String email : partnerEmails) {
        	this.data.append(",").append(this.getEmail(email));
        }

        this.data.append(")) {").append("\n")
        .append("                        if(!userAlreadyRegisteredWithOther($con,$currentUser");

        for(String email : partnerEmails) {
        	this.data.append(",").append(this.getEmail(email));
        }
        this.data.append(")) {").append("\n")
        .append("                            registerUser($con,$email,$currentUser");
        for(String email : partnerEmails) {
        	this.data.append(",").append(this.getEmail(email));
        }
        this.data.append(");").append("\n")
        .append("                        }").append("\n")
        .append("                    }").append("\n")
        .append("                }").append("\n")
        .append("                catch(PDOException $e){").append("\n")
        .append("                    echo $e->getMessage();").append("\n")
        .append("                }").append("\n")
        .append("            }").append("\n")
        .append("            else {").append("\n")
        .append("                echo json_encode(array('error' => 'partner emails not specified'));").append("\n")
        .append("            }").append("\n")
        .append("        }").append("\n");
	}

	public String getIfConditionData(String email) {
		return " isset($_GET['"+ email +"']) && !empty($_GET['"+ email +"']) ";
	}

	public String getEmail(String email) {
		return "$" + email;
	}
	public String getPartnerEmails(String email) {
		return "                " + this.getEmail(email) + " = $_GET['" + email + "'];";
	}

	public String getYourSelfOrNotData(String email, String status, boolean isFirst) {
		String ifOrElse = null;
		if(isFirst)
			ifOrElse = "if";
		else
			ifOrElse = "else if";
		StringBuilder temp = new StringBuilder();
		temp.append("                " + ifOrElse + "($" + email + " == $email) {").append("\n");
		temp.append("                    echo json_encode(array('"+status+"' => 'yourself'));").append("\n");
		temp.append("                    return;").append("\n");
		temp.append("                }");
		return temp.toString();
	}

	public String getUserQuery() {
		StringBuilder temp = new StringBuilder();
		temp.append("\"select * from "+ this.table +" where ");
		String prefix = "";
		for(int i=0; i < this.noOfParticipant; i++) {
			temp.append(prefix);
			prefix = " OR ";
			temp.append("email" + (i+1) + "=:email");
		}
		temp.append("\"");
		return temp.toString();
	}

	public String getRegisterQuery() {
		StringBuilder temp = new StringBuilder();
		temp.append("\"insert into `"+this.table+"`(");
		String prefix = "";
		for(int i=0; i < this.noOfParticipant; i++) {
			temp.append(prefix);
			prefix = ", ";
			temp.append("`email" + (i+1) + "`");
		}
		temp.append(") values('\".$email.\"'");
		for(int i=0; i < this.noOfParticipant - 1; i++) {
			temp.append(prefix);
			temp.append("NULL");
		}
		temp.append(")\"");
		return temp.toString();
	}

	//Status data

	public void addStatusData() {
		this.data.append("        else if($status == 'getstatus'){").append("\n")
		.append("            try{").append("\n")
		.append("                $con = new PDO('mysql:host='."+HOST+".';dbname='."+DB+","+DB_USER+","+DB_PASSWORD+");").append("\n")
		.append("                $con->setAttribute(PDO::ATTR_ERRMODE, PDO::ERRMODE_EXCEPTION);").append("\n")
		.append("                $check_query = $con->prepare("+ this.getCheckQuery() +");").append("\n")
		.append("                $check_query->execute();").append("\n")
		.append("                if($check_query->rowCount() == 1){").append("\n")
		.append("                    $partner = $check_query->fetch(PDO::FETCH_ASSOC);").append("\n");
		for(String email : partnerEmails) {
			this.data.append("                    " +this.getEmail(email) + " = '';").append("\n");
		}
		this.reorderPartnerRespone();
		this.outputStatus();
		this.data.append("                    return;").append("\n")
		.append("                }").append("\n")
		.append("                else if($check_query->rowCount() == 0){").append("\n")
		.append("                    echo json_encode( array('status' => 'unregistered'));").append("\n")
		.append("                    return;").append("\n")
		.append("                }").append("\n")
		.append("            }").append("\n")
		.append("            catch(PDOException $e){").append("\n")
		.append("                echo $e->getMessage();").append("\n")
		.append("            }").append("\n")
		.append("        }").append("\n");
	}

	public String getCheckQuery() {
		StringBuilder temp = new StringBuilder();
		String prefix = "";
		temp.append("\"select * from quiz where ");
		for(int i=0; i < this.noOfParticipant; i++) {
			temp.append(prefix).append("email" + (i+1) + "='\".$email.\"'");
			prefix = " OR ";
		}
		temp.append("\"");
		return temp.toString();
	}

	public void reorderPartnerRespone() {
		boolean isFirst = true;
		String ifOrElse = null;
		for(int i=0; i < this.noOfParticipant; i++) {
			if(isFirst)
				ifOrElse = "                    if";
			else
				ifOrElse = "                    else if";
			isFirst = false;
			this.data.append(ifOrElse);
			this.data.append("($partner['email"+ (i+1) +"'] == $email){").append("\n");
			for(int j=0; j < partnerEmails.size(); j++) {
				this.data.append("                        " + this.getEmail(partnerEmails.get(j)) + " = $partner['email"+ ((i+j+2)%this.noOfParticipant == 0 ? this.noOfParticipant : (i+j+2)%this.noOfParticipant) +"'];").append("\n");
			}
			this.data.append("                    }").append("\n");
		}
		
	}
	public void outputStatus() {
		this.data.append("                    echo json_encode( array('status' => 'registered'");
		for(String email : partnerEmails) {
			this.data.append(",'"+ email +"' => "+ this.getEmail(email) +"");
		}
		this.data.append("                    ));").append("\n");
	}

	//Un Register data
	public void addUnRegisterdata() {
		this.data.append("        else if($status == 'unregister'){").append("\n")
		.append("            try{").append("\n")
		.append("                $con = new PDO('mysql:host='."+HOST+".';dbname='."+DB+","+DB_USER+","+DB_PASSWORD+");").append("\n")
		.append("                $con->setAttribute(PDO::ATTR_ERRMODE, PDO::ERRMODE_EXCEPTION);").append("\n")
		.append("                $check_query = $con->prepare("+ this.getCheckQuery() +");").append("\n")
		.append("                $check_query->execute();").append("\n")
		.append("                if($check_query->rowCount() == 1){").append("\n")
		.append("                    $unregister_query = $con->prepare("+ this.getUnregisterQuery() +");").append("\n")
		.append("                    $unregister_query->bindParam(':email', $email);").append("\n")
		.append("                    $status = $unregister_query->execute();").append("\n")
		.append("                    if($status){").append("\n")
		.append("                        echo json_encode( array('status' => 'unregistered'));").append("\n")
		.append("                    }").append("\n")
		.append("                    else").append("\n")
		.append("                        echo json_encode( array('status' => 'error'));").append("\n")
		.append("                    return;").append("\n")
		.append("                }").append("\n")
		.append("                else").append("\n")
		.append("                    echo json_encode( array('status' => 'unregistered'));").append("\n")
		.append("            }").append("\n")
		.append("            catch(PDOException $e){").append("\n")
		.append("                echo $e->getMessage();").append("\n")
		.append("            }").append("\n")
		.append("        }").append("\n")
		.append("        else{").append("\n")
		.append("            echo json_encode(array('error' => 'unrecognized action'));").append("\n")
		.append("        }").append("\n")
		.append("    }").append("\n")
		.append("    else {").append("\n")
		.append("        echo json_encode(array('error' => 'action not set'));").append("\n")
		.append("    }").append("\n")
		.append("}").append("\n\n");
	}

	public String getUnregisterQuery() {
		StringBuilder temp = new StringBuilder();
		String prefix = "";
		temp.append("\"delete from " + this.table + " where ");
		for(int i=0; i < this.noOfParticipant; i++) {
			temp.append(prefix).append("email" + (i+1) + "=:email");
			prefix = " OR ";
		}
		temp.append("\"");
		return temp.toString();
	}

	public void addCheckUserExist() {
		this.data.append("function checkUserExist($con");
		for(String email : partnerEmails) {
			this.data.append(",").append(this.getEmail(email));
		}
		this.data.append(") {").append("\n");
		this.data.append("    if(");

		String prefix = "";
		for(String email : partnerEmails) {
			this.data.append(prefix);
			prefix = " && ";
			this.data.append(this.getEmail(email) + " == 'no_email'");
		}

		this.data.append(")").append("\n")
		.append("        return true;").append("\n")
		.append("    $count = 0;").append("\n");
		for(String email : partnerEmails) {
			this.data.append("    if("+ this.getEmail(email) +" == 'no_email') $count++;").append("\n");
		}
		this.data.append("    $user_exist_check = $con->prepare("+ this.getUserExistQuery() +");").append("\n");
		for(int i=0; i < partnerEmails.size(); i++) {
			this.data.append("    $user_exist_check->bindParam(':email" + (i+1) + "', "+this.getEmail(partnerEmails.get(i))+");").append("\n");
		}
		this.data.append("    $user_exist_check->execute();").append("\n")
		.append("    $totalCount = "+ (this.noOfParticipant - 1) +" - $count;").append("\n")
		.append("    if($user_exist_check->rowCount() < $totalCount) {").append("\n")
		.append("        $result = $user_exist_check->fetchAll(PDO::FETCH_ASSOC);").append("\n");
		for(String msg : partnerStatus)
			this.data.append("        $response['" + msg + "'] = 'not_registered';").append("\n");
		this.data.append("        foreach($result as $row){").append("\n");

		boolean isFirst = true;
		String ifOrElse = null;
		for(int i=0; i < partnerEmails.size(); i++) {
			if(isFirst)
				ifOrElse = "            if";
			else 
				ifOrElse = "            else if";
			isFirst = false;
			this.data.append(ifOrElse);
			this.data.append("($row['email'] == "+ this.getEmail(partnerEmails.get(i)) +")").append("\n")
			.append("                $response['"+ partnerStatus.get(i) +"'] = \"registered\";").append("\n");
		}
		this.data.append("        }").append("\n");
		for(int i=0; i < partnerEmails.size(); i++) {
			this.data.append("        if(" + partnerEmails.get(i) + " == 'no_email') unset($response['"+ partnerStatus.get(i) +"']);").append("\n");
		}
		this.data.append("        echo json_encode($response);").append("\n")
		.append("        return false;").append("\n")
		.append("    }").append("\n")
		.append("    else if($user_exist_check->rowCount() == $totalCount)").append("\n")
		.append("        return true;").append("\n")
		.append("}").append("\n");
	}

	public String getUserExistQuery() {
		StringBuilder temp = new StringBuilder();
		temp.append("\"select * from " + this.userTable + " where ");
		String prefix = "";
		for(int i=0; i < partnerEmails.size() ; i++) {
			temp.append(prefix);
			prefix = " OR ";
			temp.append("email=:email" + (i+1));
		}
		temp.append("\"");
		return temp.toString();
	}

	public void addUserAlreadyRegisteredData() {
		this.data.append("function userAlreadyRegisteredWithOther($con,$currentUser");
		for(String email : partnerEmails) {
			this.data.append(",").append(this.getEmail(email));
		}
		this.data.append(") {").append("\n")
		.append("    $existing_user_check = $con->prepare("+ this.getExistingUserQuery() +");").append("\n");
		for(int i=0; i < partnerEmails.size(); i++) {
			this.data.append("    $existing_user_check->bindParam(':email"+(i+1)+"', "+ this.getEmail(partnerEmails.get(i)) +");").append("\n");
		}
		this.data.append("    $existing_user_check->bindParam(':id', $currentUser['id']);").append("\n")
		.append("    $existing_user_check->execute();").append("\n\n")
		.append("    if($existing_user_check->rowCount() > 0) {").append("\n")
		.append("        $withWhomRegistered = $existing_user_check->fetchAll(PDO::FETCH_ASSOC);").append("\n")
		.append("        foreach($withWhomRegistered as $row){").append("\n");
		for(int i=0; i < partnerEmails.size(); i++) {
			String prefix = "";
			this.data.append("            if(");
			for(int j=0; j < noOfParticipant; j++) {
				this.data.append(prefix)
				.append("$row['email"+(j+1)+"'] == "+ this.getEmail(partnerEmails.get(i)) +"");
				prefix = " || ";
			}
			this.data.append(")").append("\n")
			.append("            {").append("\n")
			.append("                $response['"+partnerStatus.get(i)+"'] = 'already_registered_with_other';").append("\n")
			.append("            }").append("\n");
		}
		this.data.append("        }").append("\n")
		.append("        echo json_encode($response);").append("\n")
		.append("        return true;").append("\n")
		.append("    }").append("\n")
		.append("    else").append("\n")
		.append("        return false;").append("\n")
		.append("}").append("\n");
	}

	public String getExistingUserQuery() {
		StringBuilder temp = new StringBuilder();
		temp.append("\"select * from quiz where (");
		for(int i=0; i < partnerEmails.size(); i++) {
			String prefix = "";
			temp.append("(");
			for(int j=0; j < noOfParticipant; j++) {
				temp.append(prefix);
				temp.append("email" + (j+1) + "=:email" + (i+1));
				prefix = " OR ";
			}
			temp.append(")");
			if(i < partnerEmails.size() - 1)
				temp.append(" OR ");
		}
		temp.append(") AND (id <> :id)\"");
		return temp.toString();
	}

	public void addRegisterUserData() {
		this.data.append("function registerUser($con,$email,$currentUser");
		for(String email : partnerEmails) {
			this.data.append(",").append(this.getEmail(email));
		}
		this.data.append(") {").append("\n")
		.append("    $currentUserColumn = null;").append("\n");

		boolean isFirst = true;
		String ifOrElse = null;
		for(int i=0; i < noOfParticipant; i++) {
			if(isFirst)
				ifOrElse = "    if";
			else
				ifOrElse = "    else if";
			isFirst = false;
			this.data.append(ifOrElse).append("($currentUser['email"+(i+1)+"'] == $email) $currentUserColumn = "+(i+1)+";").append("\n");
		}
		for(int i=0; i < partnerEmails.size(); i++) {
			this.data.append("    $setter_email"+(i+1)+" = ($currentUserColumn + "+(i+1)+") % "+noOfParticipant+";").append("\n")
			.append("    $setter_email"+(i+1)+" == 0 ? $setter_email"+(i+1)+" = "+noOfParticipant+" : $setter_email"+(i+1)+" = $setter_email"+(i+1)+";").append("\n")
			.append("    $setter_email"+(i+1)+" = 'email'.$setter_email"+(i+1)+";").append("\n")
			.append("    "+this.getEmail(partnerEmails.get(i))+" == 'no_email' ? "+this.getEmail(partnerEmails.get(i))+" = 'NULL' : "+this.getEmail(partnerEmails.get(i))+" = \"'\"."+this.getEmail(partnerEmails.get(i))+".\"'\";").append("\n");
		}
		this.data.append("        $update_query = $con->prepare("+ this.getUpdateQuery() +");").append("\n")
		.append("    $update_query->execute();").append("\n")
		.append("    echo json_encode(array('status' => 'success'));").append("\n")
		.append("}").append("\n")
		.append("?>").append("\n");
	}

	public String getUpdateQuery() {
		StringBuilder temp = new StringBuilder();
		temp.append("\"update quiz set ");
		String prefix="";
		for(int i=0; i < partnerEmails.size(); i++) {
			temp.append(prefix);
			prefix = ", ";
			temp.append("\".$setter_email"+(i+1)+".\"=\"."+this.getEmail(partnerEmails.get(i))+".\"");
		}
		temp.append(" where id = \".$currentUser['id'].\"\"");
		return temp.toString();
	}
}