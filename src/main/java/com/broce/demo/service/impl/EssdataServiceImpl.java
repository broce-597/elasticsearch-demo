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
package com.broce.demo.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.broce.demo.entity.Essdata;
import com.broce.demo.parameter.ESSDataQueryParameter;
import com.broce.demo.service.IEssdataService;
import com.broce.demo.tool.ElasticSearchUtils;
import com.broce.demo.vo.EssdataVO;
import lombok.AllArgsConstructor;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.springblade.core.mp.support.Query;
import org.springblade.core.tool.api.R;
import org.springframework.stereotype.Service;

/**
 *  服务实现类
 *
 * @author broce
 * @since 2021-05-30
 */
@Service
@AllArgsConstructor
public class EssdataServiceImpl implements IEssdataService {

	private static String INDEX="iot_ess_sum";

	private ElasticSearchUtils elasticSearchUtils;
	/**
	 * 分页
	 * @param essdata
	 * @param parameter
	 * @param query
	 * @return
	 */
	@Override
	public R<IPage<Essdata>> page(EssdataVO essdata, ESSDataQueryParameter parameter, Query query) {
		BoolQueryBuilder matchQueryBuilder=QueryBuilders.boolQuery();

		elasticSearchUtils.genWhereToLike(matchQueryBuilder,essdata.getId(),"orderId");
		elasticSearchUtils.genWhereToDateRange(matchQueryBuilder,parameter.getAddDateStart(),parameter.getAddDateEnd(),"addDate.keyword");

		R<IPage<Essdata>> result= elasticSearchUtils.<Essdata>page(INDEX,matchQueryBuilder,query,Essdata.class);
		return result;
	}

	/**
	 * 根据id获取数据
	 * @param id
	 * @return
	 */
	@Override
	public R<Essdata> getById(Long id) {
		QueryBuilder matchQueryBuilder=QueryBuilders.termQuery("id",id);
		return elasticSearchUtils.<Essdata>getById(INDEX,matchQueryBuilder,Essdata.class);
	}
}
