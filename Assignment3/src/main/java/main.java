import java.util.Date;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public class main {

    public static void main(String[] args) {
        ThreadPoolExecutor es = (ThreadPoolExecutor) Executors.newFixedThreadPool(5);;
        LinkedBlockingDeque<Patient> listDoctor = new LinkedBlockingDeque<>();
        ArrayBlockingQueue<Patient> listDentist = new ArrayBlockingQueue<>(3);
        Clinic clinic = new Clinic(listDoctor,listDentist);
        System.out.println("Clinic is open");
        Doctor doctor = new Doctor(clinic,listDoctor);
        Dentist dentist = new Dentist(clinic,listDentist);
        Receptionist rc = new Receptionist(clinic);
        PatientGenerator pg = new PatientGenerator(clinic);
        Clock clock = new Clock(pg, doctor, dentist, rc);
        Thread thdoctor = new Thread(doctor, "GP");
        Thread thdentist = new Thread(dentist, "Dentist");
        Thread threc = new Thread(rc, "Receptionist");
        Thread thpg = new Thread(pg, "pg");
        es.execute(thpg);
        es.execute(thdoctor);
        es.execute(thdentist);
        es.execute(threc);
        es.execute(clock);
        es.shutdown();
    }
}

class Clinic {

    public boolean closingTime = false;
    public boolean Emergency = false;

    int max = 2;

    BlockingQueue<Patient> listPatient;
    BlockingDeque<Patient> listDoctor;
    BlockingQueue<Patient> listDentist;


    public Clinic(LinkedBlockingDeque<Patient> listDoctor,ArrayBlockingQueue<Patient> listDentist) {
        listPatient = new LinkedBlockingQueue<>();
        this.listDoctor=listDoctor;
        this.listDentist=listDentist;
    }
    public void add(Patient patient) {

        System.out.println("Patient : " + patient.getName() + " entering the clinic at " + patient.getInTime());

        synchronized (listPatient) {
            if (listPatient.size() == max) {
                System.out.println("Clinic full, can not registration");
                System.out.println("Patient " + patient.getName() + " Go home");

            }
            if ("Patient Emergency".equals(patient.getName())) {
                System.out.println("Emergency patient is here!!!");
                listDoctor.addLast(patient);
                Emergency = true;

            } else if (listPatient.size() < max) {
                ((LinkedBlockingQueue<Patient>) listPatient).offer(patient);
                if (listPatient.size() == 1) {
                    listPatient.notify();
                }

            }
        }
    }
    public void registration() {
        Patient patient;


        synchronized (listPatient) {
            while (listPatient.isEmpty()) {
                System.out.println("Receptionist is waiting for patient.");
                try {
                    listPatient.wait();
                } catch (InterruptedException iex) {
                }
            }
            System.out.println("There is a new patient in the queue.");
            patient = (Patient) ((LinkedBlockingQueue<?>) listPatient).poll();
        }

        Random random = new Random();
        System.out.println("Receptionist is registering : " + patient.getName());
        try {
            Thread.sleep(1000);
        } catch (InterruptedException ex) {
            Logger.getLogger(Clinic.class.getName()).log(Level.SEVERE, null, ex);
        }
        int register = random.nextBoolean() ? 0 : 1;
        if (register == 0) {
            System.out.println(patient.getName() + " want to see a GP");
            if (listDoctor.size() < 3) {
                try {
                    listDoctor.put(patient);

                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                System.out.println(patient.getName() + " is waiting for the GP");
            } else {
                System.out.println("GP queue is full");
                System.out.println(patient.getName() + " go home...");
            }
        } else {
            System.out.println(patient.getName() + " want to see a Dentist");

            if (listDentist.size() < 3) {
                try {
                    listDentist.put(patient);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                System.out.println(patient.getName() + " is waiting for the dentist");
            } else {
                System.out.println("Dentist queue is full");
                System.out.println(patient.getName() + " go home...");
            }


        }

    }

    public void see_doctor(LinkedBlockingDeque<Patient> listDoctor) {
        Patient patient;
        boolean AfterEmergency = false;
        boolean checking = false;
        boolean diagnos = false;
        boolean action = false;
        boolean prescribe = false;
        Diagnose diagnose = new Diagnose();
        if (listDoctor.size() > 0) {

            patient = (Patient) ((LinkedBlockingDeque<?>) listDoctor).poll();
            synchronized (listDoctor) {
                if (!Emergency) {
                    diagnose.see_patient(patient, listDoctor);
                    if (!Emergency) {
                        diagnose.diagnose_patient(patient, listDoctor);
                        if (!Emergency) {
                            diagnose.action_patient(patient, listDoctor);
                            if (!Emergency) {
                                diagnose.prescribe_patient(patient, listDoctor);
                                System.out.println(patient.getName() + " exits...");
                                System.out.println("There are " + listDoctor.size() + " patients left in GP Queue");

                            } else {
                                listDoctor.addFirst(patient);
                                prescribe = true;
                            }
                        } else {
                            listDoctor.addFirst(patient);
                            action = true;
                        }
                    } else {
                        listDoctor.addFirst(patient);
                        diagnos = true;
                    }
                } else {
                    listDoctor.addFirst(patient);
                    checking = true;
                }

            }

        }
        while (Emergency) {

            patient = (Patient) ((LinkedBlockingDeque<?>) listDoctor).pollLast();
            if ("Patient Emergency".equals(patient.getName())) {
                System.out.println("GP is seeing Emergency Patient");
                diagnose.see_patient(patient, listDoctor);
                diagnose.action_patient(patient, listDoctor);
                diagnose.prescribe_patient(patient, listDoctor);
                System.out.println("Emergency Patients exits...");
                if (checking) {
                    patient = (Patient) ((LinkedBlockingDeque<?>) listDoctor).poll();
                    diagnose.see_patient(patient, listDoctor);
                    diagnose.diagnose_patient(patient, listDoctor);
                    diagnose.action_patient(patient, listDoctor);
                    diagnose.prescribe_patient(patient, listDoctor);
                    checking = false;
                    Emergency = false;
                }
                if (diagnos) {
                    patient = (Patient) ((LinkedBlockingDeque<?>) listDoctor).poll();
                    diagnose.diagnose_patient(patient, listDoctor);
                    diagnose.action_patient(patient, listDoctor);
                    diagnose.prescribe_patient(patient, listDoctor);
                    diagnos = false;
                    Emergency = false;
                }
                if (action) {
                    patient = (Patient) ((LinkedBlockingDeque<?>) listDoctor).poll();
                    diagnose.action_patient(patient, listDoctor);
                    diagnose.prescribe_patient(patient, listDoctor);
                    diagnos = false;
                    Emergency = false;
                }
                if (prescribe) {
                    patient = (Patient) ((LinkedBlockingDeque<?>) listDoctor).poll();
                    diagnose.prescribe_patient(patient, listDoctor);
                    diagnos = false;
                    Emergency = false;
                } else {

                    Emergency = false;
                }
            }


        }
    }



    public void see_dentist(ArrayBlockingQueue<Patient> listDentist) {
        Patient patient;
        Diagnose diagnose = new Diagnose();
        if (listDentist.size() > 0) {
            patient = (Patient) ((ArrayBlockingQueue<?>) listDentist).poll();
            diagnose.see_patient2(patient, listDentist);
            diagnose.diagnose_patient2(patient, listDentist);
            diagnose.action_patient2(patient, listDentist);
            diagnose.prescribe_patient2(patient, listDentist);
            System.out.println(patient.getName() + " exits...");
            System.out.println("There are " + listDentist.size() + " patients left in Dentist Queue");
        }

    }

    public synchronized void setClosingTime() {
        closingTime = true;

    }
}
class Receptionist implements Runnable{


    Clinic clinic;
    main main;
    public volatile boolean closingTime = false;
    public volatile boolean closingReg = false;



    public Receptionist(Clinic clinic) {
        this.clinic = clinic;
    }


    public void run() {
        System.out.println("Receptionist get started to work");
        try {
            Thread.sleep(1500);
        } catch (InterruptedException ex) {
            Logger.getLogger(Dentist.class.getName()).log(Level.SEVERE, null, ex);
        }
        while (!closingReg){
            this.clinic.registration();
        }
        if(closingReg){
            System.out.println("It's 4 pm. Registration closed");
            try {
                Thread.sleep(4000);
            } catch (InterruptedException ex) {
                Logger.getLogger(Dentist.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

    }

    public synchronized void setClosingTime() {
        closingTime=true;
    }
    public synchronized void setClosingReg() {
        closingReg= true;


    }


}
class Dentist implements Runnable{


    Clinic clinic;
    main main;
    public boolean closingTime = false;
    BlockingQueue<Patient> listDentist;

    public Dentist(Clinic clinic, ArrayBlockingQueue<Patient> listDentist) {
        this.clinic = clinic;
        this.listDentist=listDentist;
    }



    @Override
    public void run() {
        Patient patient;
        System.out.println("Dentist get started to work");
        try {
            Thread.sleep(1500);
        } catch (InterruptedException ex) {
            Logger.getLogger(Dentist.class.getName()).log(Level.SEVERE, null, ex);
        }

        Diagnose diagnose = new Diagnose();
        System.out.println( "Dentist ready to see the patient.");
        while(!closingTime){
            clinic.see_dentist((ArrayBlockingQueue<Patient>) listDentist);
        }
        if (closingTime){
            if (closingTime){
                while(!listDentist.isEmpty()){
                    clinic.see_dentist((ArrayBlockingQueue<Patient>) listDentist);
                }
                try {

                    System.out.println("Dentist is leaving");
                    Thread.sleep(5000);
                } catch (InterruptedException ex) {
                    Logger.getLogger(Dentist.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
    }

    public synchronized void setClosingTime() {
        closingTime=true;
        try {
            Thread.sleep(500);
        } catch (InterruptedException ex) {
            Logger.getLogger(Dentist.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

}

class Doctor implements Runnable {

    Clinic clinic;
    main main;
    public boolean closingTime = false;
    BlockingDeque<Patient> listDoctor;
    int max=0;
    public Doctor(Clinic clinic,BlockingDeque listDoctor) {
        this.clinic = clinic;
        this.listDoctor=listDoctor;

    }

    @Override
    public void run() {

        System.out.println("GP get started to work");
        try {
            Thread.sleep(1000);
        } catch (InterruptedException ex) {
            Logger.getLogger(Doctor.class.getName()).log(Level.SEVERE, null, ex);
        }
        while(!closingTime){
            clinic.see_doctor((LinkedBlockingDeque<Patient>) listDoctor);
        }
        if (closingTime){
            while(!listDoctor.isEmpty()){
                clinic.see_doctor((LinkedBlockingDeque<Patient>) listDoctor);
            }

            try {
                System.out.println("Doctor is leaving");
                Thread.sleep(2000);
            } catch (InterruptedException ex) {
                Logger.getLogger(Doctor.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

    }
    public synchronized void setClosingTime() {
        closingTime=true;
        try {
            Thread.sleep(500);
        } catch (InterruptedException ex) {
            Logger.getLogger(Doctor.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

}

class Patient implements Runnable
{
    String name;
    Date inTime;
    Clinic clinic;


    public Patient(Clinic clinic)
    {
        this.clinic = clinic;
    }

    public String getName() {
        return name;
    }

    public Date getInTime() {
        return inTime;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setInTime(Date inTime) {
        this.inTime = inTime;
    }

    @Override
    public void run()
    {
        goinside();


    }
    private synchronized void goinside()
    {
        clinic.add(this);
    }

}
class PatientGenerator implements Runnable {
    Patient patient;
    Clinic clinic;
    public volatile boolean closingTime = false;



    public PatientGenerator(Clinic clinic) {
        this.clinic = clinic;
    }

    @Override
    public void run() {
        while (!closingTime) {

            try {
                Thread.sleep(500);
            } catch (InterruptedException ex) {
                Logger.getLogger(PatientGenerator.class.getName()).log(Level.SEVERE, null, ex);
            }
            Random random =new Random();
            int ep = random.nextInt(6) ;
            if(ep==5) {
                Patient patient = new Patient(clinic);
                patient.setInTime(new Date());
                Thread thpatient = new Thread(patient);
                patient.setName("Patient Emergency");
                thpatient.start();
                System.gc();
                try {
                    TimeUnit.MILLISECONDS.sleep(1000);
                } catch (InterruptedException iex) {
                }
            }else {
                Patient patient = new Patient(clinic);
                patient.setInTime(new Date());
                Thread thpatient = new Thread(patient);
                patient.setName("Patient " + thpatient.getId());
                thpatient.start();
                System.gc();
                try {
                    TimeUnit.MILLISECONDS.sleep(1000);
                } catch (InterruptedException iex) {
                }
            }


        }
        if (closingTime) {
            try {
                Thread.sleep(5000);
            } catch (InterruptedException e) {
            }

        }
    }
    public synchronized void setClosingReg() {
        closingTime = true;
    }

}


class Diagnose {

    public void see_patient(Patient patient, BlockingDeque listDoctor){
        synchronized (listDoctor){
            try {
                System.out.println("GP is checking "+patient.getName());

                TimeUnit.MILLISECONDS.sleep(1000);
            } catch (InterruptedException ex) {
                Logger.getLogger(Diagnose.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
    public void diagnose_patient(Patient patient, BlockingDeque listDoctor){
        synchronized (listDoctor){
            System.out.println("GP is diagnosing "+patient.getName());
            try {
                TimeUnit.MILLISECONDS.sleep(1000);
            } catch (InterruptedException ex) {
                Logger.getLogger(Doctor.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
    public void action_patient(Patient patient, BlockingDeque listDoctor){
        synchronized (listDoctor){
            System.out.println("GP is treating "+patient.getName());
            try {
                TimeUnit.MILLISECONDS.sleep(1000);
            } catch (InterruptedException ex) {
                Logger.getLogger(Doctor.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
    public void prescribe_patient(Patient patient, BlockingDeque listDoctor){
        synchronized (listDoctor){
            System.out.println("GP is prescribing medicines to "+patient.getName());
            try {
                TimeUnit.MILLISECONDS.sleep(1000);
            } catch (InterruptedException ex) {
                Logger.getLogger(Doctor.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    public void see_patient2(Patient patient,BlockingQueue listDentist){
        synchronized (listDentist){
            try {
                System.out.println("Dentist is checking "+patient.getName());

                TimeUnit.MILLISECONDS.sleep(1000);
            } catch (InterruptedException ex) {
                Logger.getLogger(Diagnose.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
    public void diagnose_patient2(Patient patient, BlockingQueue listDentist){
        synchronized (listDentist){
            System.out.println("Dentist is diagnosing "+patient.getName());
            try {
                TimeUnit.MILLISECONDS.sleep(1000);
            } catch (InterruptedException ex) {
                Logger.getLogger(Doctor.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
    public void action_patient2(Patient patient, BlockingQueue listDoctor){
        synchronized (listDoctor){
            System.out.println("Dentist is doing action to "+patient.getName());
            try {
                TimeUnit.MILLISECONDS.sleep(1000);
            } catch (InterruptedException ex) {
                Logger.getLogger(Doctor.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
    public void prescribe_patient2(Patient patient, BlockingQueue listDoctor){
        synchronized (listDoctor){
            System.out.println("Dentist is prescribing medicines to "+patient.getName());
            try {
                TimeUnit.MILLISECONDS.sleep(1000);
            } catch (InterruptedException ex) {
                Logger.getLogger(Doctor.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
}
class Clock extends Thread {
    PatientGenerator pg;
    Doctor doctor;
    Dentist dentist;
    Clinic clinic;
    Receptionist rc;
    int timer=20000;


    Clock(PatientGenerator pg, Doctor doctor, Dentist dentist,Receptionist rc){
        this.pg = pg;
        this.doctor = doctor;
        this.dentist=dentist;
        this.rc=rc;
    }

    public  void run(){
        try{
            Thread.sleep(this.timer);
        }catch(Exception e){
            e.printStackTrace();
        }
        synchronized(this){
            notifyClosingReg();
            notifyClosing();
        }
    }

    public synchronized void notifyClosingReg(){
        this.pg.setClosingReg();
        this.rc.setClosingReg();
        try {
            Thread.sleep(4000);
        } catch (InterruptedException ex) {
            Logger.getLogger(Dentist.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public synchronized void notifyClosing(){
        try {
            Thread.sleep(3000);
        } catch (InterruptedException ex) {
            Logger.getLogger(Clock.class.getName()).log(Level.SEVERE, null, ex);
        }
        synchronized (this){


            System.out.println("18.00 : Closing Time");
            this.doctor.setClosingTime();
            this.dentist.setClosingTime();
            this.rc.setClosingTime();

        }
    }
}












