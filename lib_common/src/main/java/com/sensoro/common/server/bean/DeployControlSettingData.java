package com.sensoro.common.server.bean;

import com.google.gson.annotations.Expose;
import com.sensoro.common.model.RecommendedTransformerValueModel;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class DeployControlSettingData implements Serializable {
    //此字段不进行序列化
    private Integer inputValue;
    //部署特殊处理
    private Integer switchSpec;
    // 线径
    private Double wireDiameter;

    // 线材  0 铜 1 铝
    private Integer wireMaterial;

    public Integer getSwitchSpec() {
        return switchSpec;
    }

    public void setSwitchSpec(Integer switchSpec) {
        this.switchSpec = switchSpec;
    }

    public Double getWireDiameter() {
        return wireDiameter;
    }

    public void setWireDiameter(Double wireDiameter) {
        this.wireDiameter = wireDiameter;
    }

    public Integer getWireMaterial() {
        return wireMaterial;
    }

    public void setWireMaterial(Integer wireMaterial) {
        this.wireMaterial = wireMaterial;
    }

    public Integer getInputValue() {
        return inputValue;
    }

    public void setInputValue(Integer inputValue) {
        this.inputValue = inputValue;
    }


    //互感器
    private Integer transformer;
    private Integer recommTrans;
    private List<wireData> input;
    private List<wireData> output;
    //推荐值
    @Expose(serialize = false, deserialize = false)
    private List<RecommendedTransformerValueModel> transformerValueList = new ArrayList<>();

    public Integer getTransformer() {
        return transformer;
    }

    public void setTransformer(Integer transformer) {
        this.transformer = transformer;
    }

    public List<wireData> getInput() {
        return input;
    }

    public void setInput(List<wireData> input) {
        this.input = input;
    }

    public List<wireData> getOutput() {
        return output;
    }

    public void setOutput(List<wireData> output) {
        this.output = output;
    }

    public Integer getRecommTrans() {
        return recommTrans;
    }

    public void setRecommTrans(Integer recommTrans) {
        this.recommTrans = recommTrans;
    }

    public List<RecommendedTransformerValueModel> getTransformerValueList() {
        return transformerValueList;
    }

    public void setTransformerValueList(List<RecommendedTransformerValueModel> transformerValueList) {
        this.transformerValueList = transformerValueList;
    }

    public static class wireData implements Serializable {
        // 线径
        private Double wireDiameter;
        // 线材  0 铜 1 铝
        private Integer wireMaterial;
        //数量
        private Integer count;

        public Double getWireDiameter() {
            return wireDiameter;
        }

        public void setWireDiameter(Double wireDiameter) {
            this.wireDiameter = wireDiameter;
        }

        public Integer getWireMaterial() {
            return wireMaterial;
        }

        public void setWireMaterial(Integer wireMaterial) {
            this.wireMaterial = wireMaterial;
        }

        public Integer getCount() {
            return count;
        }

        public void setCount(Integer count) {
            this.count = count;
        }
    }
}
