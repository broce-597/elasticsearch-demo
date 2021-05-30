/**
 * Copyright (c) 2018-2028, Chill Zhuang 庄骞 (smallchill@163.com).
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.broce.demo.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.NullSerializer;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

/**
 * 实体类
 *
 * @author broce
 * @since 2021-05-30
 */
@Data
@TableName("db_dt_essdata")
@EqualsAndHashCode(callSuper = false)
@ApiModel(value = "Essdata对象", description = "Essdata对象")
public class Essdata implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * ID
     */
    @ApiModelProperty(value = "ID")
    @TableId(value = "id", type = IdType.ASSIGN_ID)
    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;
    /**
     * 订单编号
     */
    @ApiModelProperty(value = "订单编号")
    private String OrderSn;
    /**
     * 客户id
     */
    @ApiModelProperty(value = "客户id")
    private String customerId;
    /**
     * 客户名称
     */
    @ApiModelProperty(value = "客户名称")
    private String userName;
    /**
     * 账户id
     */
    @ApiModelProperty(value = "账户id")
    private String accountId;
    /**
     * 账户变动原因
     */
    @ApiModelProperty(value = "账户变动原因")
    private String changeCause;
    /**
     * 变动前金额
     */
    @ApiModelProperty(value = "变动前金额")
    private String changeBeforeAmount;
    /**
     * 变动后金额
     */
    @ApiModelProperty(value = "变动后金额")
    private String changeAfterAmount;
    /**
     * 变动金额
     */
    @ApiModelProperty(value = "变动金额")
    private String changeAmount;
    /**
     * 支付类型
     */
    @ApiModelProperty(value = "客户id")
    private String payType;
}
