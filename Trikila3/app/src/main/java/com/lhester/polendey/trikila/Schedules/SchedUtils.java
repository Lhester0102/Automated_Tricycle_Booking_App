package com.lhester.polendey.trikila.Schedules;

import java.util.Comparator;

public class SchedUtils {
    public String getFull_name() {
        return full_name;
    }

    public void setFull_name(String full_name) {
        this.full_name = full_name;
    }

    public String getImagelink() {
        return imagelink;
    }

    public void setImagelink(String imagelink) {
        this.imagelink = imagelink;
    }

    public String getDd() {
        return dd;
    }

    public void setDd(String dd) {
        this.dd = dd;
    }

    public String getTt() {
        return tt;
    }

    public void setTt(String tt) {
        this.tt = tt;
    }

    public String getDistance() {
        return distance;
    }

    public void setDistance(String distance) {
        this.distance = distance;
    }

    public String getPlate() {
        return plate;
    }

    public void setPlate(String plate) {
        this.plate = plate;
    }

    public String getFare() {
        return fare;
    }

    public void setFare(String fare) {
        this.fare = fare;
    }

    public String getDID() {
        return DID;
    }

    public void setDID(String DID) {
        this.DID = DID;
    }

    public String get_from() {
        return _from;
    }

    public void set_from(String _from) {
        this._from = _from;
    }

    public String get_to() {
        return _to;
    }

    public void set_to(String _to) {
        this._to = _to;
    }
    public String getSID() {
        return SID;
    }

    public void setSID(String SID) {
        this.SID = SID;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public long getMinutes_remaining() {
        return minutes_remaining;
    }

    public void setMinutes_remaining(long minutes_remaining) {
        this.minutes_remaining = minutes_remaining;
    }


    private String _from;
    private String _to;
    private String full_name;
    private String imagelink;
    private String dd;
    private String tt;
    private String distance;
    private String plate;
    private String fare;
    private String DID;


    private long minutes_remaining;
    private String SID;
    private String status;
    public SchedUtils(long mr, String Status, String SID, String _from,String _to,String full_name, String imagelink, String dd, String tt, String distance, String plate, String fare, String DID) {
        this.full_name = full_name;
        this.imagelink = imagelink;
        this.dd = dd;
        this.tt = tt;
        this.distance = distance;
        this.plate = plate;
        this.fare = fare;
        this.DID = DID;
        this._from=_from;
        this._to=_to;
        this.SID=SID;
        this.status=Status;
        this.minutes_remaining=mr;
    }

    public  static Comparator<SchedUtils> minutes_asc= new Comparator<SchedUtils>() {
        @Override
        public int compare(SchedUtils o1, SchedUtils o2) {
            return (int) (o1.getMinutes_remaining() -o2.getMinutes_remaining());
        }
    };


}
