package jdbc;

import pojos.Patient;
import interfaces.PatientManager;

import java.util.List;

public class JDBCPatientManager implements PatientManager {

    @Override
    public void addPatient(Patient p) {


    }

    @Override
    public List<Patient> listPatients() {
        return List.of();
    }
}
