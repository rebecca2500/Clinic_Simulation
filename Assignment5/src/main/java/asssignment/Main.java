package asssignment;

import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.runner.RunnerException;

import java.io.IOException;
import java.util.Random;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;


class test {
    public static void main(String[] args) throws RunnerException, IOException {
        org.openjdk.jmh.Main.main(args);
    }
}



public class Main {
    @Benchmark
    @BenchmarkMode(Mode.AverageTime)
    @Warmup(iterations=20)
    @Measurement(iterations=20)
    @Fork(value=1)
    @OutputTimeUnit(TimeUnit.NANOSECONDS)


    public void main() {

        ExecutorService es = Executors.newFixedThreadPool(3);
        int patientId = 1;
        while (patientId != 10) {
            Patient patient = new Patient(patientId);
            try {
                es.submit(new PatientGen(patient));
                es.submit(new goIn(patient));
                es.submit(new Register(patient));
                es.submit(new See_Doctor(patient));
                es.submit(new See_Dentist(patient));
                es.submit(new Treating_Doctor(patient));
                es.submit(new Treating_Dentist(patient));
                es.submit(new Medicine_Doctor(patient));
                es.submit(new Medicine_Dentist(patient));
                es.submit(new Emergency(patient));
                es.submit(new See_Doc(patient));
                es.submit(new Treat_Doc(patient));
                es.submit(new Med_Doc(patient));

            } catch (Exception e) {
                e.printStackTrace();
            }
            ++patientId;
        }
    }

}


class Patient{

    int id;
    public volatile boolean Emergency=false;
    public volatile boolean Inside=false;
    public volatile boolean Registration =false;
    public volatile boolean See_Doctor=false;
    public volatile boolean See_Dentist=false;
    public volatile boolean Treating_Doctor=false;
    public volatile boolean Treating_Dentist=false;
    public volatile boolean Medicine_Doctor=false;
    public volatile boolean Medicine_Dentist=false;
    public volatile boolean wait=false;
    public volatile boolean full=false;
    public volatile boolean See_Doc=false;
    public volatile boolean Treat_Doc=false;
    public volatile boolean Med_Doc=false;

    Patient(int id){
        this.id=id;
        this.Registration=false;
        this.See_Doctor=false;
        this.See_Dentist=false;
        this.Treating_Doctor=false;
        this.Treating_Dentist=false;
        this.Medicine_Doctor=false;
        this.Medicine_Dentist=false;
        this.Inside=false;
        this.Emergency=false;
        this.wait=false;
    }



    public void setRegistration(boolean flag) { this.Registration = flag; }
    public void setInside(boolean flag) { this.Inside = flag; }
    public void setEmergency(boolean flag){this.Emergency=flag; }
    public void setSee_Doctor(boolean flag){this.See_Doctor=flag; }
    public void setSee_Dentist(boolean flag){this.See_Dentist=flag;}
    public void setTreating_Doctor(boolean flag){this.Treating_Doctor=flag;}
    public void setTreating_Dentist(boolean flag){this.Treating_Dentist=flag;}
    public void setMedicine_Doctor(boolean flag){this.Medicine_Doctor=flag;}
    public void setMedicine_Dentist(boolean flag){this.Medicine_Dentist=flag;}
    public void setSee_Doc(boolean flag) {this.See_Doc = flag; }
    public void setTreat_Doc(boolean flag) {this.Treat_Doc = flag; }
    public void setMed_Doc(boolean flag) {this.Med_Doc = flag; }

}
class PatientGen implements Callable<Patient> {
    Patient patient;
    PatientGen(Patient patient) {
        this.patient = patient;
    }

    @Override
    public Patient call() throws Exception {
        Random rand = new Random();
        int pt = rand.nextInt(6);
        if (patient.wait == false) {
            if (pt == 2) {
                //System.out.println("Patient Emergency arrive at the clinic");
                patient.setEmergency(true);
                patient.setInside(true);
                patient.wait = true;
                // Thread.sleep(2000);
            } else {
                // System.out.println("Patient " + patient.id + " arrive at the clinic");
                patient.setInside(true);
                //Thread.sleep(2000);
            }
        }return patient;
    }
}

class goIn implements Callable<Patient>{
    Patient patient;
    public int queue;
    goIn(Patient patient){
        this.patient=patient;
    }
    @Override
    public Patient call() throws Exception {
        if (patient.Inside==true) {
            //System.out.println("Patient " + patient.id + " is going inside the clinic");
            patient.setRegistration(true);
        }return patient;
    }
}
class Register implements Callable<Patient>{
    Patient patient;
    Register(Patient patient){
        this.patient=patient;
    }
    @Override
    public Patient call() throws Exception {
        if(patient.Registration==true) {
            //System.out.println("Patient " + patient.id + " is waiting for recieptionist to register");
            Random random = new Random();
            int pick = random.nextInt(2);
            if (pick == 0) {
                //System.out.println("Patient " + patient.id + " has been registered to see the dentist");
                patient.setSee_Dentist(true);

            }
            else {
                //System.out.println("Patient " + patient.id + "  has been registered to see the GP");
                patient.setSee_Doctor(true);

            }
        }return patient;
    }

}
class See_Doctor implements Callable <Patient> {
    Patient patient;

    See_Doctor(Patient patient) {
        this.patient = patient;

    }

    @Override
    public Patient call() throws Exception {
        if (patient.See_Doctor == true) {
            if (patient.wait == false) {

                //System.out.println("Patient " + patient.id + " is seeing the GP");
                patient.setTreating_Doctor(true);


            }else if (patient.wait==true){

                patient.setSee_Doc(true);


            }
        }
        return patient;
    }
}
class See_Dentist implements Callable <Patient>{
    Patient patient;
    int temp;

    See_Dentist(Patient patient){
        this.patient=patient;

    }
    @Override
    public Patient call() throws Exception {
        if(patient.See_Dentist==true) {
            // System.out.println("Patient "+patient.id+ " is seeing the Dentist");
            patient.setTreating_Dentist(true);



        }return patient;
    }
}
class Treating_Doctor implements Callable <Patient> {
    Patient patient;


    Treating_Doctor(Patient patient) {
        this.patient = patient;

    }

    @Override
    public Patient call() throws Exception {
        if (patient.Treating_Doctor == true) {
            if(patient.wait==false) {

                // System.out.println("GP is treating patient " + patient.id);
                // System.out.println("GP has finished treating patient " + patient.id);
                patient.setMedicine_Doctor(true);

            }else if (patient.wait==true){
                patient.setTreat_Doc(true);



            }

        }
        return patient;
    }
}

class Treating_Dentist implements Callable <Patient> {
    Patient patient;

    Treating_Dentist(Patient patient) {
        this.patient = patient;
    }

    @Override
    public Patient call() throws Exception {
        if(patient.Treating_Dentist==true) {

            //System.out.println("Dentist is treating patient " + patient.id);
            //System.out.println("Dentist has finished treating patient " + patient.id);
            patient.setMedicine_Dentist(true);

        }   return patient;
    }
}
class Medicine_Doctor implements Callable <Patient> {
    Patient patient;

    Medicine_Doctor(Patient patient) {
        this.patient = patient;
    }

    @Override
    public Patient call() throws Exception {
        if (patient.Medicine_Doctor == true) {
            if (patient.wait == false) {
                if(patient.id==9) {
                    //System.out.println("GP is prescribing medicine to patient " + patient.id);
                    // System.out.println("Patient " + patient.id + " exits...");
                    // Thread.sleep(500);
                    //System.exit(0);
                }else{
                    //System.out.println("GP is prescribing medicine to patient " + patient.id);
                    //System.out.println("Patient " + patient.id + " exits...");
                   // Thread.sleep(500);

                }
            }else if (patient.wait==true){
                patient.setMed_Doc(true);
                //Thread.sleep(500);

            }

        }
        return patient;
    }
}

class Medicine_Dentist implements Callable<Patient> {
    Patient patient;

    Medicine_Dentist(Patient patient) {
        this.patient = patient;
    }
    @Override
    public Patient call() throws Exception {
        if (patient.Medicine_Dentist== true) {
            //System.out.println("Dentist is prescribing medicine to patient " + patient.id);
            // System.out.println("Patient " + patient.id + " exits...");
            //Thread.sleep(500);

        }
        return patient;
    }
}



class Emergency implements Callable <Patient> {
    Patient patient;

    Emergency(Patient patient) {
        this.patient = patient;
    }

    @Override
    public Patient call() throws Exception {
        if(patient.Emergency==true) {
            //System.out.println("GP is seeing patient Emergency");
            //System.out.println("GP  is treating patient Emergency");
            //Thread.sleep(200);
            //System.out.println("GP has finished treating patient Emergency");
            patient.full=true;
            //System.out.println("Patient Emergency exits...");
            //Thread.sleep(500);

        }   return patient;
    }
}
class See_Doc implements Callable <Patient> {
    Patient patient;

    See_Doc(Patient patient) {
        this.patient = patient;

    }

    @Override
    public Patient call() throws Exception {
        if(patient.See_Doc==true) {
            while(patient.wait == true){
                if (patient.full==true) {

                    //System.out.println("Patient " + patient.id + " is seeing the GP");
                    //System.out.println("GP is treating patient " + patient.id);
                    //Thread.sleep(500);
                    //System.out.println("Patient " + patient.id + " is treated");
                    // System.out.println("GP is prescribing medicine to patient " + patient.id);
                    // System.out.println("GP has finished treating patient " + patient.id);
                    //System.out.println("Patient " + patient.id + " exits...");
                    patient.wait = false;
                    patient.full=false;
                    //Thread.sleep(500);

                }


            }
        }return patient;
    }
}
class Treat_Doc implements Callable <Patient> {
    Patient patient;

    Treat_Doc(Patient patient) {
        this.patient = patient;

    }

    @Override
    public Patient call() throws Exception {
        if (patient.Treat_Doc == true) {
            while (patient.wait == true) {
                if (patient.full == true) {
                    //System.out.println("GP is treating patient " + patient.id);
                    // Thread.sleep(500);
                    // System.out.println("Patient " + patient.id + " is treated");
                    //System.out.println("GP is prescribing medicine to patient " + patient.id);
                    // System.out.println("GP has finished treating patient " + patient.id);
                    // System.out.println("Patient " + patient.id + " exits...");
                    patient.wait = false;
                    patient.full=false;
                    //Thread.sleep(500);
                }


            }
        }
        return patient;
    }
}
class Med_Doc implements Callable <Patient> {
    Patient patient;

    Med_Doc(Patient patient) {
        this.patient = patient;

    }

    @Override
    public Patient call() throws Exception {
        if (patient.Med_Doc == true) {
            while (patient.wait == true) {
                if (patient.full == true) {

                    //System.out.println("GP is prescribing medicine to patient " + patient.id);
                    //System.out.println("GP has finished treating patient " + patient.id);
                    //System.out.println("Patient " + patient.id + " exits...");
                    patient.wait = false;
                    patient.full=false;
                    //Thread.sleep(500);
                }


            }

        }return patient;
    }
}