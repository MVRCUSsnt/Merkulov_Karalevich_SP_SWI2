import React, { createContext, Component } from "react";
import { apiFetch } from "../../api/client";
import { clearSession } from "../../utils/session";

const AuthContext = createContext(null);

class AuthProvider extends Component {
    constructor(props) {
        super(props);
        this.state = {
            user: null,
            loading: true,
        };
    }

    componentDidMount() {
        apiFetch("/api/auth/me", { method: "GET" }, { parse: "json" })
            .then((data) => {
                this.setState({ user: data, loading: false });
            })
            .then((user) => {
                this.setState({ user, loading: false });
            })
            .catch(() => {
                this.setState({ user: null, loading: false });
                clearSession();
            });
    }

    render() {
        return (
            <AuthContext.Provider value={{ user: this.state.user, loading: this.state.loading }}>
                {this.props.children}
            </AuthContext.Provider>
        );
    }
}

const AuthConsumer = AuthContext.Consumer;

export { AuthProvider, AuthConsumer, AuthContext };
