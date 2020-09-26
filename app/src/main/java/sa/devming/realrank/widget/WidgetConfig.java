package sa.devming.realrank.widget;

public class WidgetConfig {
    private int interval;
    private boolean disturb;
    private int disturbFrom;
    private int disturbTo;
    private boolean updateScreenOff;
    private int widgetId;

    public int getInterval() {
        return interval;
    }

    public void setInterval(int interval) {
        this.interval = interval;
    }

    public boolean isDisturb() {
        return disturb;
    }

    public void setDisturb(boolean disturb) {
        this.disturb = disturb;
    }

    public int getDisturbFrom() {
        return disturbFrom;
    }

    public void setDisturbFrom(int disturbFrom) {
        this.disturbFrom = disturbFrom;
    }

    public int getDisturbTo() {
        return disturbTo;
    }

    public void setDisturbTo(int disturbTo) {
        this.disturbTo = disturbTo;
    }

    public boolean isUpdateScreenOff() {
        return updateScreenOff;
    }

    public void setUpdateScreenOff(boolean updateScreenOff) {
        this.updateScreenOff = updateScreenOff;
    }

    public int getWidgetId() {
        return widgetId;
    }

    public void setWidgetId(int widgetId) {
        this.widgetId = widgetId;
    }

    @Override
    public String toString() {
        return "WidgetConfig{" +
                "interval=" + interval +
                ", disturb=" + disturb +
                ", disturbFrom=" + disturbFrom +
                ", disturbTo=" + disturbTo +
                ", updateScreenOff=" + updateScreenOff +
                ", widgetId=" + widgetId +
                '}';
    }
}
