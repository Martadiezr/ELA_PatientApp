package pojos;

import org.junit.jupiter.api.Test;
import java.sql.Date;
import java.util.ArrayList;
import static org.junit.jupiter.api.Assertions.*;

public class PatientConstructorTest {

    @Test
    void testPatientConstructorWithoutId_assignsFieldsCorrectly() {
        // Arrange
        String name = "Ana";
        String surname = "García";
        String dni = "12345678A";
        Date dob = Date.valueOf("1990-01-01");
        String sex = "F";
        Integer phone = 600123123;
        String email = "ana@example.com";
        Integer insurance = 9999;

        ArrayList<MedicalInformation> medInfo = new ArrayList<>();

        // Act
        Patient patient = new Patient(
                name,
                surname,
                dni,
                dob,
                sex,
                phone,
                email,
                insurance,
                medInfo
        );

        // Assert
        assertNull(patient.getId(), "El ID debe ser null si no se pasa en el constructor");

        assertEquals(name, patient.getName());
        assertEquals(surname, patient.getSurname());
        assertEquals(dni, patient.getDni());
        assertEquals(dob, patient.getDateOfBirth());
        assertEquals(sex, patient.getSex());
        assertEquals(phone, patient.getPhone());
        assertEquals(email, patient.getEmail());
        assertEquals(insurance, patient.getInsurance());
        assertSame(medInfo, patient.getMedicalInformation(),
                "La lista pasada debe ser la misma instancia");
    }

    @Test
    void testPatientConstructorWithId_assignsFieldsCorrectly() {
        // Arrange
        Integer id = 42;
        String name = "Luis";
        String surname = "Pérez";
        String dni = "87654321B";
        Date dob = Date.valueOf("1985-05-05");
        String sex = "M";
        Integer phone = 699999999;
        String email = "luis@example.com";
        Integer insurance = 5555;

        ArrayList<MedicalInformation> medInfo = new ArrayList<>();

        // Act
        Patient patient = new Patient(
                id,
                name,
                surname,
                dni,
                dob,
                sex,
                phone,
                email,
                insurance,
                medInfo
        );

        // Assert
        assertEquals(id, patient.getId());
        assertEquals(name, patient.getName());
        assertEquals(surname, patient.getSurname());
        assertEquals(dni, patient.getDni());
        assertEquals(dob, patient.getDateOfBirth());
        assertEquals(sex, patient.getSex());
        assertEquals(phone, patient.getPhone());
        assertEquals(email, patient.getEmail());
        assertEquals(insurance, patient.getInsurance());
        assertSame(medInfo, patient.getMedicalInformation());
    }

    @Test
    void testPatientSetters_updateFieldsCorrectly() {
        Patient patient = new Patient();

        patient.setId(10);
        patient.setName("Carlos");
        patient.setSurname("López");
        patient.setDni("00011122X");
        Date dob = Date.valueOf("2000-04-12");
        patient.setDateOfBirth(dob);
        patient.setSex("Male");
        patient.setPhone(677889900);
        patient.setEmail("carlos@test.com");
        patient.setInsurance(8888);

        assertEquals(10, patient.getId());
        assertEquals("Carlos", patient.getName());
        assertEquals("López", patient.getSurname());
        assertEquals("00011122X", patient.getDni());
        assertEquals(dob, patient.getDateOfBirth());
        assertEquals("Male", patient.getSex());
        assertEquals(677889900, patient.getPhone());
        assertEquals("carlos@test.com", patient.getEmail());
        assertEquals(8888, patient.getInsurance());
    }
}
