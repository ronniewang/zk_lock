public class Lock implements Comparable {

    private String type;

    private Integer number;

    private String path;

    public Lock(String childPath) {

        final String[] typeAndNumber = childPath.split("_");
        this.setType(typeAndNumber[0]);
        this.setNumber(Integer.valueOf(typeAndNumber[1]));
        this.setPath(Constant.LOCK_PATH + "/" + childPath);
    }

    @Override
    public int compareTo(Object o) {

        Lock other = (Lock) o;
        if (getNumber() > other.getNumber()) {
            return 1;
        } else if (getNumber() == other.getNumber()) {
            return 0;
        } else {
            return -1;
        }
    }

    @Override
    public String toString() {

        return "Lock{" +
                "type='" + getType() + '\'' +
                ", number=" + getNumber() +
                ", path='" + getPath() + '\'' +
                '}';
    }

    public String getType() {

        return type;
    }

    public void setType(String type) {

        this.type = type;
    }

    public Integer getNumber() {

        return number;
    }

    public void setNumber(Integer number) {

        this.number = number;
    }

    /**
     * child path
     */
    public String getPath() {

        return path;
    }

    public void setPath(String path) {

        this.path = path;
    }
}