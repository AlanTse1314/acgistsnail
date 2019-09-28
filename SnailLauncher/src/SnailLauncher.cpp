// Snail��������SnailLauncher
//

#include "jni.h"
#include "stdafx.h"
#include "afxwin.h"
#include "SnailLauncher.h"

// ����JVM
bool startJVM();
// ��ȡ����
char* config(LPCWSTR name);
TCHAR* configEx(LPCWSTR name);

typedef jint(JNICALL* JNICREATEPROC) (JavaVM**, void**, void*);

// SnailLauncher
BEGIN_MESSAGE_MAP(SnailLauncher, CWinApp)
END_MESSAGE_MAP()

// SnailLauncher ����
SnailLauncher::SnailLauncher() {
	// Ӧ�ó��� ID��CompanyName.ProductName.SubProduct.VersionInformation
	SetAppID(_T("acgist.Snail.SnailLauncher.1.0.0.0"));
}

// Ψһ��һ�� SnailLauncher ����
SnailLauncher launcher;

// SnailLauncher ��ʼ��
BOOL SnailLauncher::InitInstance() {
	CWinApp::InitInstance();
	EnableTaskbarInteraction(FALSE);
	startJVM();
	return TRUE;
}

int SnailLauncher::ExitInstance() {
	return CWinApp::ExitInstance();
}

// ����JVM
bool startJVM() {
	// �޸Ļ�������������java.lib.path��Ч
	SetEnvironmentVariable(_T("Path"), configEx(_T("java.path")));
	// JVM��̬��
	TCHAR* jvmPath = configEx(_T("jvm.file.path"));
	// JVM��������
	const int jvmOptionCount = 5;
	JavaVMOption jvmOptions[jvmOptionCount];
	jvmOptions[0].optionString = config(_T("model"));
	jvmOptions[1].optionString = config(_T("xms"));
	jvmOptions[2].optionString = config(_T("xmx"));
	jvmOptions[3].optionString = config(_T("file.encoding"));
	jvmOptions[4].optionString = config(_T("jar.file.path"));
	// jvmOptions[5].optionString = config(_T("java.lib.path")); // ��Чֱ�����û�������
	// ����JVM��������
	JavaVMInitArgs jvmInitArgs;
	jvmInitArgs.version = JNI_VERSION_10;
	jvmInitArgs.options = jvmOptions;
	jvmInitArgs.nOptions = jvmOptionCount;
	// �����޷�ʶ��JVM�����
	jvmInitArgs.ignoreUnrecognized = JNI_TRUE;
	// ���������࣬ע��ָ�����/�����������ã�.��
	const char startClass[] = "com/acgist/main/Application";
	// ��������������main����
	const char startMethod[] = "main";
	// ��������
	// int paramCount = 2;
	// const char* params[paramCount] = {"a", "b"};
	// ����JVM��̬���ӿ�
	HINSTANCE jvmDLL = LoadLibrary(jvmPath);
	if(jvmDLL == NULL) {
		::MessageBox(NULL, _T("JVM��̬���ӿ����ʧ��"), _T("����ʧ��"), MB_OK);
		return false;
	}
	// ��ʼ��JVM
	JNICREATEPROC jvmProcAddress = (JNICREATEPROC) GetProcAddress(jvmDLL, "JNI_CreateJavaVM");
	if(jvmDLL == NULL) {
		FreeLibrary(jvmDLL);
		::MessageBox(NULL, _T("JVM��ʼ��ʧ��"), _T("����ʧ��"), MB_OK);
		return false;
	}
	// ����JVM
	JNIEnv* env;
	JavaVM* jvm;
	jint jvmProc = (jvmProcAddress) (&jvm, (void**) &env, &jvmInitArgs);
	if(jvmProc < 0 || jvm == NULL ||env == NULL) {
		FreeLibrary(jvmDLL);
		::MessageBox(NULL, _T("JVM����ʧ��"), _T("����ʧ��"), MB_OK);
		return false;
	}
	// ����������
	jclass mainClass = env -> FindClass(startClass);
	if(env -> ExceptionCheck() == JNI_TRUE || mainClass == NULL) {
		env -> ExceptionDescribe();
		env -> ExceptionClear();
		FreeLibrary(jvmDLL);
		::MessageBox(NULL, _T("JavaMainClass����ʧ��"), _T("����ʧ��"), MB_OK);
		return false;
	}
	// ������������
	jmethodID methedID = env -> GetStaticMethodID(mainClass, startMethod, "([Ljava/lang/String;)V");
	if(env -> ExceptionCheck() == JNI_TRUE || methedID == NULL) {
		env -> ExceptionDescribe();
		env -> ExceptionClear();
		FreeLibrary(jvmDLL);
		::MessageBox(NULL, _T("JavaMainMethod����ʧ��"), _T("����ʧ��"), MB_OK);
		return false;
	}
	// ����JVM
	env -> CallStaticVoidMethod(mainClass, methedID, NULL);
	// �ͷ�JVM
	jvm -> DestroyJavaVM();
	return true;
}

// ��ȡ����
char* config(LPCWSTR name) {
	// �����ļ�����ʱ����
	CString configPath = _T("./snail.ini"), value;
	// ��ȡ���ã�config=�ڣ�name=����value=ֵ
	GetPrivateProfileString(_T("config"), name, NULL, value.GetBuffer(128), 128, configPath);
	int length = WideCharToMultiByte(CP_ACP, 0, value, -1, NULL, 0, NULL, NULL);
	char* buffer = new char[sizeof(char) * length];
	WideCharToMultiByte(CP_ACP, 0, value, -1, buffer, length, NULL, NULL);
	return buffer;
}

TCHAR* configEx(LPCWSTR name) {
	char* value = config(name);
	int length = MultiByteToWideChar(CP_ACP, 0, value, -1, NULL, 0);
	TCHAR* buffer = new TCHAR[length * sizeof(TCHAR)];
	MultiByteToWideChar(CP_ACP, 0, value, -1, buffer, length);
	delete value;
	return buffer;
}

// TCHAR -> char
/*
	int length = WideCharToMultiByte(CP_ACP, 0, value, -1, NULL, 0, NULL, NULL);
	char* buffer = new char[sizeof(char) * length];
	WideCharToMultiByte(CP_ACP, 0, value, -1, buffer, length, NULL, NULL);
*/

// char -> TCHAR
/*
	int length = MultiByteToWideChar(CP_ACP, 0, value, -1, NULL, 0);
	TCHAR* buffer = new TCHAR[length * sizeof(TCHAR)];
	MultiByteToWideChar(CP_ACP, 0, value, -1, buffer, length);
*/