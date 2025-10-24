package interfaces;

import java.util.List;
import pojos.*;
import interfaces.PatientManager;

public interface PatientManager {
    public void addPatient(Patient p);
    public List<Patient> listPatients();


}
