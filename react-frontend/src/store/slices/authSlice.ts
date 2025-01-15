import { createSlice, PayloadAction } from '@reduxjs/toolkit';
import { Auth } from '@aws-amplify/auth';

interface User {
  id: string;
  email: string;
  name?: string;
  picture?: string;
}

interface AuthState {
  isAuthenticated: boolean;
  user: User | null;
  loading: boolean;
  error: string | null;
}

const initialState: AuthState = {
  isAuthenticated: false,
  user: null,
  loading: false,
  error: null,
};

const authSlice = createSlice({
  name: 'auth',
  initialState,
  reducers: {
    setLoading: (state, action: PayloadAction<boolean>) => {
      state.loading = action.payload;
      state.error = null;
    },
    setUser: (state, action: PayloadAction<User | null>) => {
      state.user = action.payload;
      state.isAuthenticated = !!action.payload;
      state.loading = false;
      state.error = null;
    },
    setError: (state, action: PayloadAction<string>) => {
      state.error = action.payload;
      state.loading = false;
    },
    logout: (state) => {
      state.user = null;
      state.isAuthenticated = false;
      state.loading = false;
      state.error = null;
    },
  },
});

// Async thunk actions
export const checkAuthState = () => async (dispatch: any) => {
  try {
    dispatch(setLoading(true));
    const user = await Auth.currentAuthenticatedUser();
    dispatch(setUser({
      id: user.attributes.sub,
      email: user.attributes.email,
      name: user.attributes.name,
      picture: user.attributes.picture,
    }));
  } catch (error) {
    dispatch(setUser(null));
  }
};

export const signOut = () => async (dispatch: any) => {
  try {
    dispatch(setLoading(true));
    await Auth.signOut();
    dispatch(logout());
  } catch (error) {
    dispatch(setError('Failed to sign out'));
  }
};

export const { setLoading, setUser, setError, logout } = authSlice.actions;

export default authSlice.reducer;
