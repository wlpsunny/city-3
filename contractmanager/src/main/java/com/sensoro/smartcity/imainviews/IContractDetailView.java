package com.sensoro.smartcity.imainviews;

import com.sensoro.common.iwidget.IActivityIntent;
import com.sensoro.common.iwidget.IProgressDialog;
import com.sensoro.common.iwidget.IToast;
import com.sensoro.common.server.bean.ContractsTemplateInfo;

import java.util.List;

public interface IContractDetailView extends IToast,IActivityIntent,IProgressDialog {
    void setSignStatus(boolean isSigned);

    void setCustomerEnterpriseName(String customerEnterpriseName);

    void setCustomerName(String customerName);

    void setCustomerPhone(String customerPhone);

    void setCustomerAddress(String customerAddress);

    void setPlaceType(String placeType);

    void setCardIdOrEnterpriseId(String cardOrEnterpriseId);

    void setTipText(int contractType);

    void setContractCreateTime(String createdAt);

    void updateContractTemplateAdapterInfo(List<ContractsTemplateInfo> devices);

    void setServerAge(String serverAge);

    void setPeriodAge(String PeriodAge);

    void setFirstAge(String firstAge);

    void setContractTime(String time);

    void setContractNumber(String contractNumber);

    void setContractOrder(boolean isSuccess, String payTime);

    void setContractEditVisible(boolean isVisible);
}
