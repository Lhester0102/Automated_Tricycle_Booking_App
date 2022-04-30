public class TransactionClass {
    String teacherID, teacherEmail; // etc. all fieldnames in the database

    public void Teacher() {
    }

    // getters

    // setters

    @Override
    public String toString() {
        return this.teacherID + ": " + this.teacherEmail;
    }
}
