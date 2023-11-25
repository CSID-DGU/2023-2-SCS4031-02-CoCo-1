import React, { useEffect, useState } from "react";
import * as S from "./Headers.style";
import { HeadersProps } from "./Headers.type";
import { AiOutlineBell, AiOutlinePlus } from "react-icons/ai";
import { Link, useLocation } from "react-router-dom";
import ProfileImage from "../ProfileImage";
import logo from "../../assets/logo/Logo.svg"
import homeLogo from "../../assets/logo/homeLogo.svg"
import MainLogin from "../../pages/LoginPage/MainLogin";
import { useAsync } from "../../utils/API/useAsync";

const Headers: React.FC<HeadersProps> = (props: HeadersProps) => {
  let headerContent;
  const location = useLocation();
  const [isLogin, setIsLogin] = useState<boolean>(false);
  const [state, _] = useAsync({ url: "/api/user/header" });
  const isHome = props.isHome || false;
  const [isLoginModal, setIsLoginModal] = useState<boolean>(false);
  const [modalType, setModalType] = useState<string>('로그인');
  const [userData, setUserData] = useState<any>({});
  const [propsData, setPropsData] = useState<any>(null);

  useEffect(() => {
    if(state.error) {
      console.log(state.error);
      sessionStorage.setItem("accountId", "");
      sessionStorage.setItem("profileImg", "");
    }
    else {
    if(state.data){
      if(state.data.message&&state.data.message==="not login") {
        setIsLogin(false);
        sessionStorage.setItem("accountId", "");
        sessionStorage.setItem("profileImg", "");
      }
      else {
        setIsLogin(true);
        setUserData(state.data);
        sessionStorage.setItem("accountId", state.data.id);
        sessionStorage.setItem("profileImg", state.data.profileImg);
      }
    }
  }
  }, [state]);

  if (isLogin) {
    if (location.pathname.includes('/myfeed/plans/')) {
      headerContent = (
        <Link to="/myfeed/plans/add">
          <S.HeaderText>일정추가</S.HeaderText>
        </Link>
      );
    } else if (location.pathname.includes('/post/places/')) {
      headerContent = (
        <Link to="/post/places/add">
          <S.HeaderText>장소추가</S.HeaderText>
        </Link>
      );
    } else {
      headerContent = (
        <Link to="/mypage/plans/add">
          <S.HeaderText>일정추가</S.HeaderText>
        </Link>
      );
    }
  }

  useEffect(() => {
    const queryString = window.location.search;
    const params = new URLSearchParams(queryString);
    const reIdValue:any = params.get('reId');
    const reIdResult = decodeURIComponent(reIdValue);
    const dataValue:any = params.get('data');
    const dataResult = decodeURIComponent(dataValue);
    const tokenValue:any = params.get('token');
    const tokenResult = decodeURIComponent(tokenValue);
    setPropsData({
      registrationId : reIdResult,
      message: dataResult,
      token: tokenResult
    }
    )

    // 상태를 업데이트
    if (dataResult === "id 입력이 필요합니다") {
      setIsLoginModal(!isLoginModal);
      setModalType('회원가입');
    }
  }, []);

  if(propsData !== null) {
  return (
    <>
      {isLogin ? (
        <S.Header isHome={isHome}>
          <Link to={"/"}>
            <S.LogoImg>
              {isHome ? (
                <img src={homeLogo} alt="logo" width="100%" height="100%" />
              ) : (
                <img src={logo} alt="logo" width="100%" height="100%" />
              )}
            </S.LogoImg>
          </Link>
          <S.RightWrapper>
            <S.HeaderIconText>
              <AiOutlinePlus size="1.1rem" />
              {headerContent}
            </S.HeaderIconText>
            <S.HeaderIconText>
              <AiOutlineBell size="1.1rem" />
              <S.HeaderText>알림</S.HeaderText>
              {props.alarmCount && props.alarmCount !== 0 && (
                <S.AlarmBadge>{props.alarmCount}</S.AlarmBadge>
              )}
            </S.HeaderIconText>
            <Link to="/myfeed">
              <ProfileImage type="small" margin="0 0 0 1.5rem" src={userData.profileImg}/>
            </Link>
          </S.RightWrapper>
        </S.Header>
      ) : (
        <S.Header isHome={isHome}>
          <Link to={"/"}>
          <S.LogoImg>
            {isHome ? (
              <img src={homeLogo} alt="logo" width="100%" height="100%" />
            ) : (
              <img src={logo} alt="logo" width="100%" height="100%" />
            )}
          </S.LogoImg>
          </Link>
          <S.RightWrapper>
            <S.HeaderText onClick={() => setIsLoginModal(!isLoginModal)} >로그인</S.HeaderText>
          </S.RightWrapper>
        </S.Header>
      )}

      {
        isLoginModal && <MainLogin type={modalType} data={propsData} onClose={() => setIsLoginModal(!isLoginModal)} />
      }
    </>


  )
    }
};

export default Headers;