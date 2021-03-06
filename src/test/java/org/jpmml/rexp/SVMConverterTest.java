/*
 * Copyright (c) 2017 Villu Ruusmann
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

import org.dmg.pmml.FieldName;
import org.junit.Test;

public class SVMConverterTest extends ConverterTest {

	@Test
	public void evaluateFormulaAudit() throws Exception {
		evaluate("LibSVMFormula", "Audit");
	}

	@Test
	public void evaluateAnomalyFormulaAudit() throws Exception {
		evaluate("LibSVMAnomalyFormula", "Audit", excludeFields(SVMConverterTest.decisionFunctionFields));
	}

	@Test
	public void evaluateFormulaAuto() throws Exception {
		evaluate("LibSVMFormula", "Auto");
	}

	@Test
	public void evaluateAuto() throws Exception {
		evaluate("LibSVM", "Auto");
	}

	@Test
	public void evaluateFormulaIris() throws Exception {
		evaluate("LibSVMFormula", "Iris");
	}

	@Test
	public void evaluateAnomalyFormulaIris() throws Exception {
		evaluate("LibSVMAnomalyFormula", "Iris", excludeFields(SVMConverterTest.decisionFunctionFields));
	}

	@Test
	public void evaluateIris() throws Exception {
		evaluate("LibSVM", "Iris");
	}

	private static final FieldName[] decisionFunctionFields = {FieldName.create("decisionFunction")};
}