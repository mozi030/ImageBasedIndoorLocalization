#include<iostream>
using namespace std;
#include<time.h>
#include<stdlib.h>

#include<fstream>
#include<queue>
#include<vector>
#include<cstring>
#include<algorithm>
#include<stack>

#include <android/log.h>

struct tree_node{
	float**node_data;
	tree_node* left_tree_node;
	tree_node* right_tree_node;
	int split_col;
	float split_median_value;
	int node_row;
	int node_col;
};

static queue<tree_node*>del_tree_node;
static queue<float*>del_row;
static queue<float**>del_data;

struct nearest_distance_node{
	int node_num;
	float this_distance;
	nearest_distance_node(int a_node_num, float a_distance){
		node_num = a_node_num;
		this_distance = a_distance;
	}
};

class MyMatch{
public:
	static void buildKDTree();

	static void readKDTree();

	static void del_all();

	//float* searchKDTree(float*search_data);
	vector<nearest_distance_node*> searchKDTree(float*search_data);

	static tree_node* getRootNode();

	static float compute_distance(float*a, float*b);

	~MyMatch();
private:
	static int dataCol;
	static int index;
	static int file_num;//ֻ������2,4,8
	static int height;
	static tree_node* root_node;

	//readKDTree
	static const char * kdtree_file;
	static tree_node* json2tree(Json::Value json_node, int layer);

	//buildKDTree
	static const char * kdtree_file2;
	static int kdtree_file2_num;//�ļ���39
	static int kdtree_file2_feature_num;//ÿ���ļ���������100
	static float**allData;
	static void find_split(tree_node* a_tree_node);
	static bool comp(float*a, float*b);
	static void find_median(tree_node* a_tree_node);
	static void split_tree(tree_node* a_tree_node);
	static void build_tree(tree_node* a_tree_node);

	//search
	stack<tree_node*>search_trace;
	float* nearest_node;
	float nearest_distance;
	stack<bool>search_path;

	void dfs_search(float*search_data, tree_node* a_tree_node);
	void trace_back_check_nearest(float* search_data);
	void update_nearest(float current_distance, float*current_node);

	queue<float*>my_del1;

	vector<nearest_distance_node*>nearest_distance_nodes;
	int need_nearest_num;
	queue<nearest_distance_node*>del_nearest_distance_node;
};

tree_node* MyMatch::getRootNode(){
	return root_node;
}

int MyMatch::dataCol = 64;
int MyMatch::index = 0;
int MyMatch::file_num = 8;//ֻ������2,4,8
int MyMatch::height = 2;
tree_node* MyMatch::root_node = NULL;
const char* MyMatch::kdtree_file = "/sdcard/data/mozi/kdtree_file/tree_file";
const char* MyMatch::kdtree_file2 = "/sdcard/data/manyImages/data_v2/filterFeatures_500/";
int MyMatch::kdtree_file2_num = 84;
int MyMatch::kdtree_file2_feature_num = 500;
float** MyMatch::allData = NULL;

void MyMatch::find_split(tree_node* a_tree_node){
	float temp;

	//��ÿ��ƽ��ֵ
	float* average_in_every_col = new float[a_tree_node->node_col];
	memset(average_in_every_col, 0, a_tree_node->node_col * sizeof(float));
	del_row.push(average_in_every_col);
	for (int i = 0; i < a_tree_node->node_row; i++) {
		for (int j = 0; j < a_tree_node->node_col; j++) {
			average_in_every_col[j] += a_tree_node->node_data[i][j];
		}
	}
	for (int j = 0; j < a_tree_node->node_col; j++) {
		average_in_every_col[j] /= a_tree_node->node_row;
	}

	//��ÿ�еķ���
	float* deviation_in_every_col = new float[a_tree_node->node_col];
	memset(deviation_in_every_col, 0, a_tree_node->node_col * sizeof(float));
	del_row.push(deviation_in_every_col);
	for (int i = 0; i < a_tree_node->node_row; i++) {
		for (int j = 0; j < a_tree_node->node_col; j++) {
			temp = (a_tree_node->node_data[i][j] - average_in_every_col[j]);
			deviation_in_every_col[j] += temp * temp;
		}
	}

	//ȡ�������ֵ
	float biggest_deviation = -100000;
	int biggest_deviation_col;
	for (int j = 0; j < a_tree_node->node_col - 1; j++) {//���һ��ֻ�б��
		//deviation_in_every_col[j] /= a_tree_node->node_row;
		if (deviation_in_every_col[j] > biggest_deviation) {
			biggest_deviation = deviation_in_every_col[j];
			biggest_deviation_col = j;
		}
	}

	//���յó����ڷ��ѵ���
	a_tree_node->split_col = biggest_deviation_col;
}

float**data_temp;
int split_temp;
bool MyMatch::comp(float*a, float*b) {
	return a[split_temp] < b[split_temp];
}

void MyMatch::find_median(tree_node* a_tree_node) {
	split_temp = a_tree_node->split_col;

	data_temp = a_tree_node->node_data;
	std::sort(data_temp, data_temp + a_tree_node->node_row, comp);

	a_tree_node->split_median_value = data_temp[a_tree_node->node_row / 2][split_temp];
}

void MyMatch::split_tree(tree_node* a_tree_node) {
	//�½�������
	tree_node* new_left_tree = new tree_node();
	a_tree_node->left_tree_node = new_left_tree;
	del_tree_node.push(new_left_tree);

	//new_left_tree->father_tree_node = a_tree_node;
	new_left_tree->node_col = a_tree_node->node_col;
	new_left_tree->node_row = a_tree_node->node_row / 2;
	new_left_tree->node_data = new float*[new_left_tree->node_row];
	del_data.push(new_left_tree->node_data);

	for (int i = 0; i < a_tree_node->node_row / 2; i++) {
		new_left_tree->node_data[i] = a_tree_node->node_data[i];
	}

	//�½�������
	tree_node* new_right_tree = new tree_node();
	a_tree_node->right_tree_node = new_right_tree;
	del_tree_node.push(new_right_tree);

	//new_right_tree->father_tree_node = a_tree_node;
	new_right_tree->node_col = a_tree_node->node_col;
	new_right_tree->node_row = a_tree_node->node_row - new_left_tree->node_row;
	new_right_tree->node_data = new float*[new_right_tree->node_row];
	del_data.push(new_right_tree->node_data);

	for (int i = a_tree_node->node_row / 2, ii = 0; i < a_tree_node->node_row; i++, ii++) {
		new_right_tree->node_data[ii] = a_tree_node->node_data[i];
	}
}

void MyMatch::build_tree(tree_node* a_tree_node) {
	if (a_tree_node->node_row > 1) {
		//cout << "��ǰ���Ĳ���: " << a_tree_node->height;
		//��Ҷ�ӽڵ�������
		//Ѱ���������ѵ���
		find_split(a_tree_node);
		//cout << "    ѡ��ķ��ѵ���: " << a_tree_node->split_col << endl;
		//num[a_tree_node->split_col]++;

		//Ѱ�Ҹ��е���λ��
		find_median(a_tree_node);

		//������λ��������
		split_tree(a_tree_node);

		//����������������
		build_tree(a_tree_node->left_tree_node);
		build_tree(a_tree_node->right_tree_node);
	}
	else {
		//Ҷ�ӽڵ�������
		a_tree_node->left_tree_node = a_tree_node->right_tree_node = NULL;
	}
}

void MyMatch::buildKDTree(){
	allData = new float*[kdtree_file2_num * kdtree_file2_feature_num];
	del_data.push(allData);
	int ii = 0;
	char c;
	//__android_log_print(ANDROID_LOG_INFO, "JNITag", "start reading .SURF.features");
	for (int n = 1; n <= kdtree_file2_num; n++) {
		string num = "";
		if (n < 10) {
			num += char('0' + n);
		}
		else {
			num += char('0' + n / 10);
			num += char('0' + n % 10);
		}
		string path = kdtree_file2 + num + ".SURF.features";
		ifstream fin;
		fin.open(path.c_str());
		for (int i = 0; i < kdtree_file2_feature_num; i++) {
			allData[ii] = new float[dataCol + 1];
			del_row.push(allData[ii]);
			for (int j = 0; j < dataCol - 1; j++) {
				fin >> allData[ii][j];
				fin >> c;
			}
			fin >> allData[ii][dataCol - 1];
			allData[ii][dataCol] = n;
			ii++;
		}
	}
	//__android_log_print(ANDROID_LOG_INFO, "JNITag", "finish reading .SURF.features");

	root_node = new tree_node();
	del_tree_node.push(root_node);
	root_node->node_row = kdtree_file2_num * kdtree_file2_feature_num;
	root_node->node_col = dataCol + 1;
	root_node->node_data = allData;

	//__android_log_print(ANDROID_LOG_INFO, "JNITag", "start building tree");
	build_tree(root_node);
	//__android_log_print(ANDROID_LOG_INFO, "JNITag", "finish building tree");
}

void MyMatch::readKDTree() {
	ifstream fin;
	fin.open(string(string(kdtree_file) + ".json").c_str(), ios::binary);
	Json::Reader reader;
	Json::Value root;
	reader.parse(fin,root);
	fin.close();

	root_node = MyMatch::json2tree(root, 0);
}

void MyMatch::del_all(){
	float*temp;
	while (!del_row.empty()) {
		temp = del_row.front();
		del_row.pop();
		delete[] temp;
	}

	float**temp2;
	while (!del_data.empty()) {
		temp2 = del_data.front();
		del_data.pop();
		delete[]temp2;
	}

	tree_node*temp3;
	while (!del_tree_node.empty()) {
		temp3 = del_tree_node.front();
		del_tree_node.pop();
		delete temp3;
	}
	root_node = NULL;
}

tree_node* MyMatch::json2tree(Json::Value json_node, int layer){
	if(layer != height) {
		tree_node* a_tree_node = new tree_node();
		del_tree_node.push(a_tree_node);
		a_tree_node->node_col = json_node["col"].asInt();
		a_tree_node->node_row = json_node["row"].asInt();
		a_tree_node->split_col = json_node["split"].asInt();
		a_tree_node->split_median_value = json_node["median"].asDouble();
		if (a_tree_node->node_row > 1) {
			a_tree_node->left_tree_node = json2tree(json_node["left"], layer + 1);
			a_tree_node->right_tree_node = json2tree(json_node["right"], layer + 1);
		} else {
			a_tree_node->left_tree_node = a_tree_node->right_tree_node = NULL;
			a_tree_node->node_data = new float*[1];
			del_data.push(a_tree_node->node_data);
			a_tree_node->node_data[0] = new float[a_tree_node->node_col];
			del_row.push(a_tree_node->node_data[0]);
			for (int j =0; j < a_tree_node->node_col;j++) {
				a_tree_node->node_data[0][j] = json_node["data"][j].asDouble();
			}
		}
		return a_tree_node;
	} else {
		tree_node* a_tree_node = new tree_node();
		del_tree_node.push(a_tree_node);
		a_tree_node->node_col = json_node["col"].asInt();
		a_tree_node->node_row = json_node["row"].asInt();
		a_tree_node->split_col = json_node["split"].asInt();
		a_tree_node->split_median_value = json_node["median"].asDouble();

		ifstream fin1;
		fin1.open(string(string(kdtree_file) + char('0' + index++) + ".json").c_str(), ios::binary);
		Json::Reader reader1;
		Json::Value json_node1;
		reader1.parse(fin1,json_node1);
		fin1.close();
		a_tree_node->left_tree_node = json2tree(json_node1, layer + 1);

		ifstream fin2;
		fin2.open(string(string(kdtree_file) + char('0' + index++) + ".json").c_str(), ios::binary);
		Json::Reader reader2;
		Json::Value json_node2;
		reader2.parse(fin2,json_node2);
		fin2.close();
		a_tree_node->right_tree_node = json2tree(json_node2, layer + 1);

		return a_tree_node;
	}
}

float MyMatch::compute_distance(float*a, float*b) {
	float result = 0, temp;
	for (int i = 0; i < dataCol; i++) {
		temp = a[i] - b[i];
		result += temp * temp;
	}
	return sqrt(result);
}

bool nearest_distance_nodes_fun (nearest_distance_node* a,nearest_distance_node*b) {
	return a->this_distance < b->this_distance;
}
void MyMatch::update_nearest(float current_distance, float*current_node){
//		__android_log_print(ANDROID_LOG_INFO, "javaCheck", "1");
	if (nearest_distance_nodes.size() < need_nearest_num) {

		nearest_distance_node* a_node = new nearest_distance_node((int)current_node[dataCol],current_distance);

//		__android_log_print(ANDROID_LOG_INFO, "javaCheck", "2");
		del_nearest_distance_node.push(a_node);

//		__android_log_print(ANDROID_LOG_INFO, "javaCheck", "3");
		nearest_distance_nodes.push_back(a_node);
	} else {

//		__android_log_print(ANDROID_LOG_INFO, "javaCheck", "4");
		if (current_distance < nearest_distance_nodes[need_nearest_num - 1]->this_distance) {
//			__android_log_print(ANDROID_LOG_INFO, "javaCheck", "5");
			nearest_distance_node* a_node = new nearest_distance_node((int)current_node[dataCol],current_distance);
			del_nearest_distance_node.push(a_node);
			nearest_distance_nodes[need_nearest_num - 1] = a_node;

//			__android_log_print(ANDROID_LOG_INFO, "javaCheck", "6");
		}
	}
	sort(nearest_distance_nodes.begin(),nearest_distance_nodes.end(),nearest_distance_nodes_fun);
	nearest_distance = nearest_distance_nodes[nearest_distance_nodes.size() - 1]->this_distance;
//	__android_log_print(ANDROID_LOG_INFO, "javaCheck", "9");
}

void MyMatch::dfs_search(float*search_data, tree_node* a_tree_node){
	if (a_tree_node->node_row > 1) {
		search_trace.push(a_tree_node);
		if (search_data[a_tree_node->split_col] < a_tree_node->split_median_value) {
			search_path.push(true);
			dfs_search(search_data, a_tree_node->left_tree_node);
		}
		else {
			search_path.push(false);
			dfs_search(search_data, a_tree_node->right_tree_node);
		}
	}
	else {
		float temp = compute_distance(search_data, a_tree_node->node_data[0]);
//		__android_log_print(ANDROID_LOG_INFO, "javaCheck", "update begin");
		update_nearest(temp, a_tree_node->node_data[0]);
//		__android_log_print(ANDROID_LOG_INFO, "javaCheck", "update end");
//		if (nearest_distance > temp) {
//			nearest_node = a_tree_node->node_data[0];
//			nearest_distance = temp;
//		}
	}
}

void MyMatch::trace_back_check_nearest(float* search_data) {
	if (search_trace.empty()) {
		return;
	}
	else {
		tree_node* a_tree_node = search_trace.top();
		search_trace.pop();
		bool last_choice = search_path.top();
		search_path.pop();

		if (nearest_distance <= fabs(a_tree_node->split_median_value - search_data[a_tree_node->split_col])) {
			trace_back_check_nearest(search_data);
		}
		else {
			if (last_choice == true){
				dfs_search(search_data, a_tree_node->right_tree_node);
				trace_back_check_nearest(search_data);
			}
			else {
				dfs_search(search_data, a_tree_node->left_tree_node);
				trace_back_check_nearest(search_data);
			}
		}
	}
}

//float* MyMatch::searchKDTree(float*search_data) {
vector<nearest_distance_node*> MyMatch::searchKDTree(float*search_data) {

	//__android_log_print(ANDROID_LOG_INFO, "javaCheck", "search begin");
	while (!search_trace.empty()) {
		search_trace.pop();
	}
	while (!search_path.empty()) {
		search_path.pop();
	}
	while (!my_del1.empty()) {
		float*a = my_del1.front();
		delete []a;
		my_del1.pop();
	}
	nearest_distance_nodes.clear();

	//ʹ��dfs��kd-tree�����ҵ�Ҷ�ӽڵ�
	need_nearest_num = 1;
	nearest_distance = 10000000;
	dfs_search(search_data, root_node);

	//����
	//trace_back_check_nearest(search_data);
	while (!search_trace.empty()) {
		tree_node* a_tree_node = search_trace.top();
		search_trace.pop();
		bool last_choice = search_path.top();
		search_path.pop();

		if (nearest_distance > fabs(a_tree_node->split_median_value - search_data[a_tree_node->split_col])) {
			if (last_choice == true){
				dfs_search(search_data, a_tree_node->right_tree_node);
			}
			else {
				dfs_search(search_data, a_tree_node->left_tree_node);
			}
		}
	}

	return nearest_distance_nodes;

//	float* answer = new float[2];
//	answer[0] = nearest_node[dataCol];
//	answer[1] = nearest_distance;
//	return answer;
}

MyMatch::~MyMatch(){
//	__android_log_print(ANDROID_LOG_INFO, "javaCheck", "7");
	while (!my_del1.empty()) {
		float*a = my_del1.front();
		delete []a;
		my_del1.pop();
	}
	nearest_distance_node*temp4;
	while(!del_nearest_distance_node.empty()) {
		temp4 = del_nearest_distance_node.front();
		del_nearest_distance_node.pop();
		delete temp4;
	}
//	__android_log_print(ANDROID_LOG_INFO, "javaCheck", "8");
}








