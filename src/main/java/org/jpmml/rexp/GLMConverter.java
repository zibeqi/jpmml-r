/*
 * Copyright (c) 2016 Villu Ruusmann
 *
 * This file is part of JPMML-R
 *
 * JPMML-R is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * JPMML-R is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with JPMML-R.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.jpmml.rexp;

import java.util.List;

import org.dmg.pmml.DataField;
import org.dmg.pmml.MiningFunction;
import org.dmg.pmml.Model;
import org.dmg.pmml.OpType;
import org.dmg.pmml.Value;
import org.dmg.pmml.general_regression.GeneralRegressionModel;
import org.jpmml.converter.Feature;
import org.jpmml.converter.ModelUtil;
import org.jpmml.converter.PMMLUtil;
import org.jpmml.converter.Schema;
import org.jpmml.converter.general_regression.GeneralRegressionModelUtil;

public class GLMConverter extends LMConverter {

	public GLMConverter(RGenericVector glm){
		super(glm);
	}

	@Override
	public void encodeFeatures(FeatureMapper featureMapper){
		RGenericVector glm = getObject();

		RGenericVector family = (RGenericVector)glm.getValue("family");
		RGenericVector model = (RGenericVector)glm.getValue("model");

		RStringVector familyFamily = (RStringVector)family.getValue("family");

		super.encodeFeatures(featureMapper);

		GeneralRegressionModel.Distribution distribution = parseFamily(familyFamily.asScalar());
		switch(distribution){
			case BINOMIAL:
				DataField dataField = featureMapper.getTargetField();

				dataField.setOpType(OpType.CATEGORICAL);

				RNumberVector<?> variable = (RNumberVector<?>)model.getValue((dataField.getName()).getValue());
				if(!(variable instanceof RIntegerVector)){
					throw new IllegalArgumentException();
				}

				RIntegerVector factor = (RIntegerVector)variable;

				List<Value> values = dataField.getValues();
				if(values.size() > 0){
					throw new IllegalArgumentException();
				}

				values.addAll(PMMLUtil.createValues(factor.getLevelValues()));
				break;
			default:
				break;
		}
	}

	@Override
	public Model encodeModel(Schema schema){
		RGenericVector glm = getObject();

		RDoubleVector coefficients = (RDoubleVector)glm.getValue("coefficients");
		RGenericVector family = (RGenericVector)glm.getValue("family");

		Double intercept = coefficients.getValue(LMConverter.INTERCEPT, true);

		RStringVector familyFamily = (RStringVector)family.getValue("family");
		RStringVector familyLink = (RStringVector)family.getValue("link");

		List<Feature> features = schema.getFeatures();

		if(coefficients.size() != (features.size() + (intercept != null ? 1 : 0))){
			throw new IllegalArgumentException();
		}

		List<Double> featureCoefficients = getFeatureCoefficients(features, coefficients);

		String targetCategory = null;

		List<String> targetCategories = schema.getTargetCategories();
		if(targetCategories != null && targetCategories.size() > 0){

			if(targetCategories.size() != 2){
				throw new IllegalArgumentException();
			}

			targetCategory = targetCategories.get(1);
		}

		MiningFunction miningFunction = (targetCategory != null ? MiningFunction.CLASSIFICATION : MiningFunction.REGRESSION);

		GeneralRegressionModel generalRegressionModel = new GeneralRegressionModel(GeneralRegressionModel.ModelType.GENERALIZED_LINEAR, miningFunction, ModelUtil.createMiningSchema(schema), null, null, null)
			.setDistribution(parseFamily(familyFamily.asScalar()))
			.setLinkFunction(parseLinkFunction(familyLink.asScalar()))
			.setLinkParameter(parseLinkParameter(familyLink.asScalar()));

		GeneralRegressionModelUtil.encodeRegressionTable(generalRegressionModel, features, intercept, featureCoefficients, targetCategory);

		switch(miningFunction){
			case CLASSIFICATION:
				generalRegressionModel.setOutput(ModelUtil.createProbabilityOutput(schema));
				break;
			default:
				break;
		}

		return generalRegressionModel;
	}

	static
	private GeneralRegressionModel.Distribution parseFamily(String family){

		switch(family){
			case "binomial":
				return GeneralRegressionModel.Distribution.BINOMIAL;
			case "gaussian":
				return GeneralRegressionModel.Distribution.NORMAL;
			case "Gamma":
				return GeneralRegressionModel.Distribution.GAMMA;
			case "inverse.gaussian":
				return GeneralRegressionModel.Distribution.IGAUSS;
			case "poisson":
				return GeneralRegressionModel.Distribution.POISSON;
			default:
				throw new IllegalArgumentException(family);
		}
	}

	static
	private GeneralRegressionModel.LinkFunction parseLinkFunction(String link){

		switch(link){
			case "cloglog":
				return GeneralRegressionModel.LinkFunction.CLOGLOG;
			case "identity":
				return GeneralRegressionModel.LinkFunction.IDENTITY;
			case "inverse":
				return GeneralRegressionModel.LinkFunction.POWER;
			case "log":
				return GeneralRegressionModel.LinkFunction.LOG;
			case "logit":
				return GeneralRegressionModel.LinkFunction.LOGIT;
			case "probit":
				return GeneralRegressionModel.LinkFunction.PROBIT;
			case "sqrt":
				return GeneralRegressionModel.LinkFunction.POWER;
			default:
				throw new IllegalArgumentException(link);
		}
	}

	static
	private Double parseLinkParameter(String link){

		switch(link){
			case "inverse":
				return -1d;
			case "sqrt":
				return (1d / 2d);
			default:
				return null;
		}
	}
}