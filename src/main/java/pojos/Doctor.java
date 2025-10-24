package pojos;

import java.util.Date;
import java.util.Objects;

public class Doctor {
    private Integer id;
    private String name;
    private String surname;
    private String dni;
    private Date dateOfBirth;
    private String sex;
    private String email;

    public Doctor(Integer id, String name, String surname, String dni, Date dateOfBirth, String sex, String email) {
        this.id = id;
        this.name = name;
        this.surname = surname;
        this.dni = dni;
        this.dateOfBirth = dateOfBirth;
        this.sex = sex;
        this.email = email;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
    public String getSurname() {
        return surname;
    }
    public void setSurname(String surname) {
        this.surname = surname;
    }
    public String getDni() {
        return dni;
    }
    public void setDni(String dni) {
        this.dni = dni;
    }
    public Date getDateOfBirth() {
        return dateOfBirth;
    }
    public void setDateOfBirth(Date dateOfBirth) {
        this.dateOfBirth = dateOfBirth;
    }

    public String getSex() {
        return sex;
    }

    public void setSex(String sex) {
        this.sex = sex;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        Doctor doctor = (Doctor) o;
        return id == doctor.id && Objects.equals(name, doctor.name) && Objects.equals(surname, doctor.surname) && Objects.equals(dni, doctor.dni) && Objects.equals(dateOfBirth, doctor.dateOfBirth) && Objects.equals(sex, doctor.sex) && Objects.equals(email, doctor.email);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, surname, dni, dateOfBirth, sex, email);
    }

    @Override
    public String toString() {
        return "Doctor{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", surname='" + surname + '\'' +
                ", dni='" + dni + '\'' +
                ", dateOfBirth=" + dateOfBirth +
                ", sex='" + sex + '\'' +
                ", email='" + email + '\'' +
                '}';
    }

}
