package org.dromara.easyai.matrixTools;

public interface CudaMatrix {
	void init() throws Exception;
	void softMax(Matrix matrix) throws Exception;

	Matrix matrixSoftMaxPd(Matrix qkt, Matrix errorMatrix, double wordVectorDimension) throws Exception;

	Matrix mulMatrix(Matrix matrix1, Matrix matrix2) throws Exception;

	// 矩阵数加
	void mathAdd(Matrix matrix, float nub) throws Exception;

	// 矩阵数减
	void mathSub(Matrix matrix, float nub) throws Exception;

	// 矩阵数乘
	void mathMul(Matrix matrix, float nub) throws Exception;

	// 矩阵数除
	void mathDiv(Matrix matrix, float nub) throws Exception;
}
