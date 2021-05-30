package com.broce.demo.parameter;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

/**
 * ESS数据查询参数实体类
 */
@Data
@EqualsAndHashCode(callSuper = false)
@ApiModel(value = "ESS数据查询参数实体类", description = "ESS数据查询参数实体类")
public class ESSDataQueryParameter extends QueryParameter {
    private static final long serialVersionUID = 1L;
}
