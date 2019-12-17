package com.lpz.dragerview.launcher;

import java.util.List;
import java.util.Objects;

public class HomeDataBean {
    private DataBean dataBean;
    private FolderBean folderBean;

    public static class FolderBean{
       private List<DataBean> nameList;

        public List<DataBean> getNameList() {
            return nameList;
        }

        public void setNameList(List<DataBean> nameList) {
            this.nameList = nameList;
        }
    }

    public DataBean getDataBean() {
        return dataBean;
    }

    public void setDataBean(DataBean dataBean) {
        this.dataBean = dataBean;
    }

    public FolderBean getFolderBean() {
        return folderBean;
    }

    public void setFolderBean(FolderBean folderBean) {
        this.folderBean = folderBean;
    }

    public static class DataBean{
        private String name;
        private boolean isEmpty;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public boolean isEmpty() {
            return isEmpty;
        }

        public void setEmpty(boolean empty) {
            isEmpty = empty;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof DataBean)) return false;
            DataBean dataBean = (DataBean) o;
            return Objects.equals(getName(), dataBean.getName());
        }

        @Override
        public int hashCode() {

            return Objects.hash(getName());
        }
    }
}
